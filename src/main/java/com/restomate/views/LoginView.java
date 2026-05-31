package com.restomate.views;

import com.restomate.controllers.LoginController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;

public class LoginView {

        // Root sekarang HBox (dua panel: kiri + kanan)
        private HBox root;
        private LoginController controller;

        public LoginView() {
                controller = new LoginController(this);
                buildUI();
        }

        private void buildUI() {
                root = new HBox();

                // =============================================
                // PANEL KIRI — Branding Restoran
                // =============================================
                VBox leftPanel = new VBox(20);
                leftPanel.setAlignment(Pos.CENTER);
                leftPanel.setPrefWidth(300);
                leftPanel.setMinWidth(300);
                leftPanel.setPadding(new Insets(35));
                leftPanel.setBackground(new Background(new BackgroundFill(
                                Color.web("#2C1A0E"), CornerRadii.EMPTY, Insets.EMPTY)));

                Label lblIcon = new Label("🍽️");
                lblIcon.setFont(Font.font("Segoe UI", 42));

                Label lblTitle = new Label("RestoMate");
                lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
                lblTitle.setTextFill(Color.web("#F39C12"));

                // Garis dekoratif
                Rectangle divider = new Rectangle(60, 3);
                divider.setFill(Color.web("#C0392B"));

                Label lblTagline = new Label("Kelola Restoran Anda\ndengan Mudah & Cepat");
                lblTagline.setFont(Font.font("Segoe UI", 12));
                lblTagline.setTextFill(Color.web("#BDC3C7"));
                lblTagline.setStyle("-fx-text-alignment: center;");
                lblTagline.setAlignment(Pos.CENTER);

                Label lblFooter = new Label("© 2026 RestoMate");
                lblFooter.setFont(Font.font("Segoe UI", 11));
                lblFooter.setTextFill(Color.web("#7F8C8D"));

                leftPanel.getChildren().addAll(lblIcon, lblTitle, divider, lblTagline, lblFooter);

                // =============================================
                // PANEL KANAN — Form Login
                // =============================================
                VBox rightPanel = new VBox();
                rightPanel.setAlignment(Pos.CENTER);
                rightPanel.setBackground(new Background(new BackgroundFill(
                                Color.web("#FDF6EC"), CornerRadii.EMPTY, Insets.EMPTY)));
                HBox.setHgrow(rightPanel, Priority.ALWAYS);

                // Card form
                VBox card = new VBox(10);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(28));
                card.setMaxWidth(310);
                card.setBackground(new Background(new BackgroundFill(
                                Color.WHITE, new CornerRadii(14), Insets.EMPTY)));

                DropShadow shadow = new DropShadow();
                shadow.setColor(Color.color(0, 0, 0, 0.12));
                shadow.setRadius(20);
                shadow.setOffsetY(6);
                card.setEffect(shadow);

                Label lblFormTitle = new Label("Selamat Datang 👋");
                lblFormTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
                lblFormTitle.setTextFill(Color.web("#2C1A0E"));

                Label lblFormSub = new Label("Masuk untuk memulai shift Anda");
                lblFormSub.setFont(Font.font("Segoe UI", 13));
                lblFormSub.setTextFill(Color.web("#888888"));
                lblFormSub.setPadding(new Insets(0, 0, 10, 0));

                // Input Username
                Label lblUser = new Label("Username");
                lblUser.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                lblUser.setTextFill(Color.web("#555555"));

                String inputStyle = "-fx-pref-height: 34px; -fx-font-size: 12px; " +
                                "-fx-background-radius: 8; -fx-border-radius: 8; " +
                                "-fx-border-color: #DDD; -fx-background-color: #FAFAFA;";
                String inputFocusStyle = "-fx-pref-height: 34px; -fx-font-size: 12px; " +
                                "-fx-background-radius: 8; -fx-border-radius: 8; " +
                                "-fx-border-color: #C0392B; -fx-border-width: 2; -fx-background-color: white;";

                TextField txtUsername = new TextField();
                txtUsername.setPromptText("Masukkan username...");
                txtUsername.setMaxWidth(Double.MAX_VALUE);
                txtUsername.setStyle(inputStyle);
                txtUsername.focusedProperty().addListener(
                                (obs, old, focused) -> txtUsername.setStyle(focused ? inputFocusStyle : inputStyle));

                // Input Password
                Label lblPass = new Label("Password");
                lblPass.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                lblPass.setTextFill(Color.web("#555555"));

                PasswordField txtPassword = new PasswordField();
                txtPassword.setPromptText("Masukkan password...");
                txtPassword.setMaxWidth(Double.MAX_VALUE);
                txtPassword.setStyle(inputStyle);
                txtPassword.focusedProperty().addListener(
                                (obs, old, focused) -> txtPassword.setStyle(focused ? inputFocusStyle : inputStyle));

                // Label Error
                Label errorLabel = new Label();
                errorLabel.setTextFill(Color.web("#E74C3C"));
                errorLabel.setFont(Font.font("Segoe UI", 12));
                errorLabel.setVisible(false);

                // Tombol Login
                Button btnLogin = new Button("🔑  Masuk");
                btnLogin.setMaxWidth(Double.MAX_VALUE);
                String btnStyle = "-fx-background-color: #C0392B; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-pref-height: 36px; " +
                                "-fx-background-radius: 8; -fx-font-size: 12;";
                String btnHoverStyle = "-fx-background-color: #A93226; -fx-text-fill: white; " +
                                "-fx-font-weight: bold; -fx-pref-height: 36px; " +
                                "-fx-background-radius: 8; -fx-font-size: 12; -fx-cursor: hand;";
                btnLogin.setStyle(btnStyle);
                btnLogin.setOnMouseEntered(e -> btnLogin.setStyle(btnHoverStyle));
                btnLogin.setOnMouseExited(e -> btnLogin.setStyle(btnStyle));

                // Enter di kolom password juga bisa login
                txtPassword.setOnAction(e -> btnLogin.fire());

                btnLogin.setOnAction(
                                e -> controller.handleLogin(txtUsername.getText(), txtPassword.getText(), errorLabel));

                card.getChildren().addAll(
                                lblFormTitle, lblFormSub,
                                lblUser, txtUsername,
                                lblPass, txtPassword,
                                errorLabel, btnLogin);

                rightPanel.getChildren().add(card);
                root.getChildren().addAll(leftPanel, rightPanel);
        }

        // Return type HBox karena root sudah HBox
        public HBox getView() {
                return root;
        }
}
