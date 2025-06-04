package org.example.storeprogram;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {
    public static BorderPane mainLayout;
    public static int userId;


    @Override
    public void start(Stage stage){

        mainLayout = new BorderPane();
        mainLayout.setCenter(LoginClass.loginForm());
        mainLayout.getStyleClass().add("root");

        Scene scene = new Scene(mainLayout, 600, 400);


        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }


    public static void main(String[] args) {
        launch();
    }
}