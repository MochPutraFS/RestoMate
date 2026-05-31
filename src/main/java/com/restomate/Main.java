package com.restomate;

import com.restomate.utils.Database;
import com.restomate.views.LoginView;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene mainScene;
    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;

        Database.initializeDatabase();

        LoginView loginView = new LoginView();

        mainScene = new Scene(loginView.getView(), 1024, 700);

        mainScene.getRoot().setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif;");

        primaryStage.setTitle("RestoMate - Restaurant Management System");
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }

    public static void setRoot(Parent root) {
        mainScene.setRoot(root);
        root.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
    }

    public static void closeApp() {
        mainStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
