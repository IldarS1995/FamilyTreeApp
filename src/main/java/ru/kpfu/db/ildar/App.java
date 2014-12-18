package ru.kpfu.db.ildar;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.kpfu.db.ildar.view.Controller;

/** Main class that launches the JavaFX application */
public class App extends Application
{
    public static void main( String[] args )
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Controller.primaryStage = stage;

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainWindow.fxml"));

        stage.setTitle("Family Tree Application");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}
