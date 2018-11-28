package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.model.Person;
import sample.view.PersonEditDialogController;
import sample.view.PersonOverviewController;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<Person> personData = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
//        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("The Server");
//        primaryStage.setScene(new Scene(root, 600, 300));
//        primaryStage.show();
        fillData();
        initRootLayout();
        showPersonOverview();
    }

    private void fillData() {
        this.personData.add(new Person("Heinz", "Kurz"));
        this.personData.add(new Person("Ruth", "Mueller"));
        this.personData.add(new Person("Hans", "Muster"));
        this.personData.add(new Person("Cornelia", "Meier"));
        this.personData.add(new Person("Werner", "Meyer"));
        this.personData.add(new Person("Lydia", "Kunz"));
        this.personData.add(new Person("Anna", "Best"));
        this.personData.add(new Person("Stefan", "Meier"));
        this.personData.add(new Person("Martin", "Mueller"));
    }

    private void initRootLayout() {
        try {
            // Загружаем корневой макет из fxml файла.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/RootLayout.fxml"));
            this.rootLayout = loader.load();

            // Отображаем сцену, содержащую корневой макет.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showPersonOverview() {
        try {
            // Загружаем сведения об адресатах.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/PersonOverview.fxml"));
            AnchorPane personOverview = loader.load();

            // Помещаем сведения об адресатах в центр корневого макета.
            this.rootLayout.setCenter(personOverview);

            // Даём контроллеру доступ к главному приложению.
            PersonOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    /**
     * Возвращает данные в виде наблюдаемого списка адресатов.
     * @return data-set
     */
    public ObservableList<Person> getPersonData() {
        return personData;
    }

    public static void main(String[] args) {
        MyServer srv = new MyServer();
        srv.start_session();
        launch(args);
        srv.stop_session();
    }

    /**
     * Открывает диалоговое окно для изменения деталей указанного адресата.
     * Если пользователь кликнул OK, то изменения сохраняются в предоставленном
     * объекте адресата и возвращается значение true.
     *
     * @param person - объект адресата, который надо изменить
     * @return true, если пользователь кликнул OK, в противном случае false.
     */
    public boolean showPersonEditDialog(Person person) {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("view/PersonEditDialog.fxml"));
            AnchorPane page = loader.load();

            // Создаём диалоговое окно Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Person");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Передаём адресата в контроллер.
            PersonEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setPerson(person);

            // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
