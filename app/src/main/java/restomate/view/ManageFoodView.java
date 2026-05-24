package restomate.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import restomate.controller.FoodController;
import restomate.model.Makanan;
import restomate.model.MenuRestoran;

public class ManageFoodView {
    private VBox view;
    private FoodController controller;
    private TableView<MenuRestoran> table;
    private ObservableList<MenuRestoran> foodList;
    private FilteredList<MenuRestoran> filteredFoodList;

    private TextField txtId, txtNama, txtHarga, txtStok, txtKategori, txtSearch;
    private TextArea txtDeskripsi;

    public ManageFoodView() {
        controller = new FoodController();
        createView();
        loadData();
    }

    private void createView() {
        view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #F4F7FC;");

        Label titleLabel = new Label("Kelola Data Makanan & Minuman");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Form Section
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20));
        formGrid.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        txtId = new TextField();
        txtId.setPromptText("ID (Auto-generated)");
        txtId.setDisable(true);
        txtId.setPrefWidth(200);

        txtNama = new TextField();
        txtNama.setPromptText("Masukkan Nama");

        txtHarga = new TextField();
        txtHarga.setPromptText("Masukkan Harga (Misal: 15000)");

        txtStok = new TextField();
        txtStok.setPromptText("Masukkan Jumlah Stok");

        txtKategori = new TextField();
        txtKategori.setPromptText("Masukkan Kategori (Bebas)");

        txtDeskripsi = new TextArea();
        txtDeskripsi.setPromptText("Masukkan Deskripsi");
        txtDeskripsi.setPrefRowCount(3);
        txtDeskripsi.setWrapText(true);

        formGrid.add(new Label("ID:"), 0, 0); formGrid.add(txtId, 1, 0);
        formGrid.add(new Label("Nama:"), 0, 1); formGrid.add(txtNama, 1, 1);
        formGrid.add(new Label("Harga:"), 2, 0); formGrid.add(txtHarga, 3, 0);
        formGrid.add(new Label("Stok:"), 2, 1); formGrid.add(txtStok, 3, 1);
        formGrid.add(new Label("Kategori:"), 0, 2); formGrid.add(txtKategori, 1, 2);
        formGrid.add(new Label("Deskripsi:"), 2, 2); formGrid.add(txtDeskripsi, 3, 2);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button btnTambah = new Button("Tambah");
        Button btnUpdate = new Button("Update");
        Button btnHapus = new Button("Hapus");
        Button btnClear = new Button("Bersihkan Form");

        String btnStyle = "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px 15px; -fx-background-radius: 5;";
        btnTambah.setStyle(btnStyle + "-fx-background-color: #2ECC71;");
        btnUpdate.setStyle(btnStyle + "-fx-background-color: #F39C12;");
        btnHapus.setStyle(btnStyle + "-fx-background-color: #E74C3C;");
        btnClear.setStyle(btnStyle + "-fx-background-color: #95A5A6;");

        btnTambah.setOnAction(e -> handleTambah());
        btnUpdate.setOnAction(e -> handleUpdate());
        btnHapus.setOnAction(e -> handleHapus());
        btnClear.setOnAction(e -> clearForm());

        buttonBox.getChildren().addAll(btnTambah, btnUpdate, btnHapus, btnClear);
        formGrid.add(buttonBox, 0, 3, 4, 1);

        // Search Section
        txtSearch = new TextField();
        txtSearch.setPromptText("Cari Makanan...");
        txtSearch.setPrefHeight(35);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredFoodList != null) {
                filteredFoodList.setPredicate(menu -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    return menu.getNama().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        // Table Section
        table = new TableView<>();
        
        TableColumn<MenuRestoran, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MenuRestoran, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));

        TableColumn<MenuRestoran, Double> colHarga = new TableColumn<>("Harga (Rp)");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));

        TableColumn<MenuRestoran, Integer> colStok = new TableColumn<>("Sisa Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));

        TableColumn<MenuRestoran, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategori"));

        TableColumn<MenuRestoran, String> colDeskripsi = new TableColumn<>("Deskripsi");
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));

        table.getColumns().addAll(colId, colNama, colHarga, colStok, colKategori, colDeskripsi);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        view.getChildren().addAll(titleLabel, formGrid, txtSearch, table);
    }

    private void loadData() {
        foodList = FXCollections.observableArrayList(controller.getAllFoods());
        filteredFoodList = new FilteredList<>(foodList, p -> true);
        table.setItems(filteredFoodList);
    }

    private void handleTambah() {
        try {
            String nama = txtNama.getText();
            double harga = Double.parseDouble(txtHarga.getText());
            int stok = Integer.parseInt(txtStok.getText());
            String kategori = txtKategori.getText();
            String deskripsi = txtDeskripsi.getText();
            
            MenuRestoran menu;
            // Defaults to Makanan if logic doesn't specify, but admin types category freely now.
            // Using Makanan as a generic wrapper or we could use a concrete non-abstract Menu class.
            // But let's keep Makanan for now and use 0 for pedas level as default.
            menu = new Makanan(0, nama, harga, stok, deskripsi, kategori, 0);

            if (controller.addFood(menu)) {
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal menyimpan data ke database.");
            }
        } catch (Exception e) {
            showAlert("Input Tidak Valid", "Mohon periksa kembali tipe data yang Anda masukkan (Harga/Stok harus angka).");
        }
    }

    private void handleUpdate() {
        try {
            if (txtId.getText().isEmpty()) {
                showAlert("Peringatan", "Pilih data dari tabel terlebih dahulu untuk di-update.");
                return;
            }
            int id = Integer.parseInt(txtId.getText());
            String nama = txtNama.getText();
            double harga = Double.parseDouble(txtHarga.getText());
            int stok = Integer.parseInt(txtStok.getText());
            String kategori = txtKategori.getText();
            String deskripsi = txtDeskripsi.getText();

            MenuRestoran menu = new Makanan(id, nama, harga, stok, deskripsi, kategori, 0);

            if (controller.updateFood(menu)) {
                clearForm();
                loadData();
            } else {
                showAlert("Error", "Gagal memperbarui data.");
            }
        } catch (Exception e) {
            showAlert("Input Tidak Valid", "Mohon periksa kembali tipe data yang Anda masukkan.");
        }
    }

    private void handleHapus() {
        if (txtId.getText().isEmpty()) {
            showAlert("Peringatan", "Pilih data dari tabel terlebih dahulu untuk dihapus.");
            return;
        }
        int id = Integer.parseInt(txtId.getText());
        if (controller.deleteFood(id)) {
            clearForm();
            loadData();
        } else {
            showAlert("Error", "Gagal menghapus data.");
        }
    }

    private void populateForm(MenuRestoran menu) {
        txtId.setText(String.valueOf(menu.getId()));
        txtNama.setText(menu.getNama());
        txtHarga.setText(String.valueOf(menu.getHarga()));
        txtStok.setText(String.valueOf(menu.getStok()));
        txtKategori.setText(menu.getKategori());
        txtDeskripsi.setText(menu.getDeskripsi());
    }

    private void clearForm() {
        txtId.clear();
        txtNama.clear();
        txtHarga.clear();
        txtStok.clear();
        txtKategori.clear();
        txtDeskripsi.clear();
        table.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}