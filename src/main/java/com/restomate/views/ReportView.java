package com.restomate.views;

import com.restomate.controllers.ReportController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;

public class ReportView {

    private BorderPane root;
    private ReportController controller;

    private Label lblIncome;
    private Button btnCetak, btnReset;

    // ── Warna Tema Restoran ──
    private static final String CLR_PRIMARY = "#C0392B";
    private static final String CLR_BG      = "#FDF6EC";
    private static final String CLR_DARK    = "#2C1A0E";
    private static final String CLR_BORDER  = "#E8DDD0";

    public ReportView() {
        root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                Color.web(CLR_BG), CornerRadii.EMPTY, Insets.EMPTY)));

        // Header
        root.setTop(buildHeader());

        // Konten tengah
        StackPane centerWrapper = new StackPane();
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.getChildren().add(buildReportCard());
        root.setCenter(centerWrapper);

        controller = new ReportController(this);
    }

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
                Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        header.setStyle("-fx-border-color: " + CLR_BORDER + "; -fx-border-width: 0 0 1 0;");

        Label lblTitle = new Label("📊 Laporan Harian");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        lblTitle.setTextFill(Color.web(CLR_DARK));

        header.getChildren().add(lblTitle);
        return header;
    }

    private VBox buildReportCard() {
        VBox card = new VBox(0);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setBackground(new Background(new BackgroundFill(
                Color.WHITE, new CornerRadii(16), Insets.EMPTY)));

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.1));
        shadow.setRadius(24);
        shadow.setOffsetY(6);
        card.setEffect(shadow);

        // ── Header kartu (strip berwarna atas) ──
        VBox cardTop = new VBox(8);
        cardTop.setPadding(new Insets(20, 28, 18, 28));
        cardTop.setAlignment(Pos.CENTER);
        cardTop.setBackground(new Background(new BackgroundFill(
                Color.web(CLR_DARK), new CornerRadii(16, 16, 0, 0, false), Insets.EMPTY)));

        Label lblIcon = new Label("💰");
        lblIcon.setFont(Font.font("Segoe UI", 32));

        Label lblTitle = new Label("Omset Hari Ini");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTitle.setTextFill(Color.web("#BDC3C7"));

        cardTop.getChildren().addAll(lblIcon, lblTitle);

        // ── Angka Omset ──
        VBox cardMid = new VBox(6);
        cardMid.setPadding(new Insets(20, 28, 14, 28));
        cardMid.setAlignment(Pos.CENTER);

        lblIncome = new Label("Rp 0");
        lblIncome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        lblIncome.setTextFill(Color.web("#27AE60")); // Hijau cuan

        Label lblSub = new Label("Total transaksi yang berhasil diproses hari ini");
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setTextFill(Color.web("#999999"));
        lblSub.setWrapText(true);
        lblSub.setStyle("-fx-text-alignment: center;");

        // Garis pemisah dekoratif
        Rectangle divider = new Rectangle(80, 3);
        divider.setFill(Color.web(CLR_PRIMARY));

        cardMid.getChildren().addAll(lblIncome, divider, lblSub);

        // ── Tombol Aksi ──
        HBox buttonBox = new HBox(12);
        buttonBox.setPadding(new Insets(14, 28, 22, 28));
        buttonBox.setAlignment(Pos.CENTER);

        btnCetak = new Button("🖨️ Cetak Laporan");
        btnCetak.setPrefWidth(148);
        String cetakNormal = "-fx-background-color: #EBF5FB; -fx-text-fill: #1A5276; -fx-font-weight: bold; -fx-pref-height: 36px; -fx-background-radius: 10; -fx-border-color: #AED6F1; -fx-border-radius: 10; -fx-font-size: 12;";
        String cetakHover  = "-fx-background-color: #2980B9; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 36px; -fx-background-radius: 10; -fx-font-size: 12; -fx-cursor: hand;";
        btnCetak.setStyle(cetakNormal);
        btnCetak.setOnMouseEntered(e -> btnCetak.setStyle(cetakHover));
        btnCetak.setOnMouseExited(e -> btnCetak.setStyle(cetakNormal));

        btnReset = new Button("🔒 Tutup Kasir");
        btnReset.setPrefWidth(148);
        String resetNormal = "-fx-background-color: " + CLR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 36px; -fx-background-radius: 10; -fx-font-size: 12;";
        String resetHover  = "-fx-background-color: #A93226; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-height: 36px; -fx-background-radius: 10; -fx-font-size: 12; -fx-cursor: hand;";
        btnReset.setStyle(resetNormal);
        btnReset.setOnMouseEntered(e -> btnReset.setStyle(resetHover));
        btnReset.setOnMouseExited(e -> btnReset.setStyle(resetNormal));

        buttonBox.getChildren().addAll(btnCetak, btnReset);

        card.getChildren().addAll(cardTop, cardMid, buttonBox);
        return card;
    }

    // ── Getter ──
    public BorderPane getView()        { return root; }
    public Label getLblIncome()        { return lblIncome; }
    public Button getBtnCetak()        { return btnCetak; }
    public Button getBtnReset()        { return btnReset; }
    public ReportController getController() { return controller; }
}
