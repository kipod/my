using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using System.Windows.Threading;

namespace MyChat
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        private int lastMessageId = 0;

        public MainWindow()
        {
            InitializeComponent();
            //LoadChatContext();
            var timer = new DispatcherTimer();
            timer.Tick += (sender, args) =>
            {
                LoadChatContext();
            };
            timer.Interval = new TimeSpan(0,0,2);
            timer.Start();
        }
        
        private void LoadChatContext()
        {
            try
            {
                using (var db = new ChatContext())
                {
                    int newlastMsgId = 0;
                    var set = from msg in db.Messages
                        orderby msg.MessageId descending
                        select msg.MessageId;
                    if (set.Any())
                    {
                        newlastMsgId = set.First();
                    }
                    
                    if (lastMessageId < newlastMsgId)
                    {
                        lastMessageId = newlastMsgId;

                        var load = from m in db.Messages select new { m.User.Name, m.Text };
                        dataGrid.ItemsSource = load.ToList();
                        if (dataGrid.Items.Count > 0)
                        {
                            //var border = VisualTreeHelper.GetChild(dataGrid, 0) as Decorator;
                            //var scroll = border?.Child as ScrollViewer;
                            //scroll?.ScrollToEnd();
                            dataGrid.ScrollIntoView(load.ToList().Last());
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                if (ex.InnerException != null) MessageBox.Show(ex.InnerException.ToString());
                throw;
            }
        }

        private void buttonSend_Click(object sender, RoutedEventArgs e)
        {
            if (textBox.Text.Length == 0)
            {
                return;
            }

            var machineName = System.Environment.MachineName;

            try
            {
                using (var db = new ChatContext())
                {
                    var name = machineName;
                    var user = from u in db.Users
                        where u.MachineName == name
                        select u;

                    User usr = null;
                    if (user.Any())
                    {
                        machineName = user.ToString();
                        usr = user.First();
                    }
                    else
                    {
                        usr = new User
                        {
                            Name = machineName,
                            MachineName = machineName
                        };

                        db.Users.Add(usr);
                    }

                    var msg = new Message
                    {
                        Text = textBox.Text,
                        User = usr
                    };

                    db.Messages.Add(msg);
                    db.SaveChanges();
                }

            }
            catch (Exception ex)
            {
                if (ex.InnerException != null) MessageBox.Show(ex.InnerException.ToString());
                throw;
            }
            textBox.Text = string.Empty;
            //LoadChatContext();
            Keyboard.Focus(textBox);

        }
    }
}
