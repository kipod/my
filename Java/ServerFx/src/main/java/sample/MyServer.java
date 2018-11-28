package sample;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

public class MyServer implements Runnable {
    private final static int STRING_LENGTH_HEADER = 4;
    private final static String HOST_NAME = "0.0.0.0";
    private final static int SERVER_PORT = 5656;
    private final static int MAX_BUF_SIZE = 4096;

    private static ByteBuffer buffer = ByteBuffer.allocate(MAX_BUF_SIZE);
    private boolean bRun = false;
    private Thread thread = null;
    private Selector selector = null;

    void start_session() {
        try {
            thread = new Thread(this, "network_worker");
            bRun = true;
            thread.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void stop_session() {
        bRun = false;
        if(selector != null)
        {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (thread != null){
            try {
                thread.join(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }

    }

    private static void log(String str) {
        System.out.println(str);
    }

    private String readString(SocketChannel channel) {
        buffer.position(0);
        int readBytes = -1;
        int strLen = 0;
        buffer.limit(STRING_LENGTH_HEADER);
        try {
            while (buffer.position() < buffer.limit()) {
                readBytes = channel.read(buffer);
                if (readBytes == -1) break;
            }
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            strLen = buffer.getInt(0);
            buffer.limit(STRING_LENGTH_HEADER + strLen);
            while (buffer.position() < buffer.limit()) {
                readBytes = channel.read(buffer);
                if (readBytes == -1) break;
            }
        } catch (IOException e){
            log(e.toString());
            readBytes = -1;
        }
        if (readBytes == -1) {
            return null;
        }
        return new String(buffer.array(), STRING_LENGTH_HEADER, strLen, Charset.forName("UTF-8"));
    }

    private void writeString(SocketChannel channel, String str) throws IOException {
        byte[] bytes = str.getBytes(Charset.forName("UTF-8"));
        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer);
        buffer.limit(buffer.capacity());
        buffer.position(0);
    }

    private void doWork() throws IOException {
        // Selector: multiplexor of SelectableChannel objects
        this.selector = Selector.open(); // selector is open here
        // ServerSocketChannel: selectable channel for stream-oriented listening sockets
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        // Binds the channel's socket to a local address and configures the socket to listen for connections
        serverSocket.bind(new InetSocketAddress(HOST_NAME, SERVER_PORT));
        // Adjusts this channel's blocking mode.
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, serverSocket.validOps(), null);

        // Keep server running
        while (this.bRun) {
            //log("i'm a server and i'm waiting for new connection and buffer select...");
            // Selects a set of keys whose corresponding channels are ready for I/O operations
            this.selector.select();
            if(!this.selector.isOpen()){
                break;
            }
            // token representing the registration of a SelectableChannel with a Selector
            for (SelectionKey key : selector.selectedKeys())
                if (key.isAcceptable()) {
                    SocketChannel socket = serverSocket.accept();
                    if (socket != null) {
                        // Adjusts this channel's blocking mode to false
                        socket.configureBlocking(false);

                        // Operation-set bit for read operations
                        socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        log(String.format("Connection accepted on: '%s' from: '%s'", socket.getLocalAddress(), socket.getRemoteAddress()));
                    }
                    // Tests whether this key's channel is ready for reading and writing
                } else if (key.isReadable() && key.isWritable()) {
                    SocketChannel socket = (SocketChannel) key.channel();
                    String str = readString(socket);
                    if (str == null) {
                        socket.finishConnect();
                        key.cancel();
                        log(String.format("Disconnected from:'%s'", socket.getRemoteAddress().toString()));
                    } else {
                        SocketAddress remoteAddress = socket.getRemoteAddress();
                        log(String.format("Received '%s' from:'%s'", str, remoteAddress.toString()));
                        str = str.trim();
                        // Emulate work time
                        try {
                            TimeUnit.MILLISECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            log(e.toString());
                        }
                        log(String.format("Send '%s' to:'%s'", str, remoteAddress));
                        writeString(socket, str);
                    }
                }
        }
    }

    @Override
    public void run() {
        try {
            doWork();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
