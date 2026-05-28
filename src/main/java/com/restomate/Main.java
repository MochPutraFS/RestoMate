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

        // Inisialisasi database saat aplikasi nyala
        Database.initializeDatabase();

        // Mulai dari halaman Login
        LoginView loginView = new LoginView();

        mainScene = new Scene(loginView.getView(), 1024, 700);

        // Tema font global — background diatur per halaman
        mainScene.getRoot().setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif;");

        primaryStage.setTitle("RestoMate - Restaurant Management System");
        primaryStage.setScene(mainScene);
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    /**
     * Fungsi untuk pindah halaman dari mana saja.
     * Contoh: Main.setRoot(new LoginView().getView());
     */
    public static void setRoot(Parent root) {
        mainScene.setRoot(root);
        // Pertahankan font global, background diatur oleh masing-masing view
        root.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");
    }

    public static void closeApp() {
        mainStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
