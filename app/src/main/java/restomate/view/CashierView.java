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
import restomate.controller.CashierController;
import restomate.model.CartItem;
import restomate.model.MenuRestoran;
import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CashierView {
    private VBox view;
    private CashierController controller;
    
    private TableView<MenuRestoran> tableMenu;
    private ObservableList<MenuRestoran> menuList;
    private FilteredList<MenuRestoran> filteredMenuList;

    private TableView<CartItem> tableCart;
    private ObservableList<CartItem> cartList;
    
    private TextField txtSearch;
    private Label lblTotal;
    private double totalHarga = 0.0;

    public CashierView() {
        controller = new CashierController();
        cartList = FXCollections.observableArrayList();
        createView();
        loadMenuData();
    }

    private void createView() {
        view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #F4F7FC;");

        Label titleLabel = new Label("Menu Kasir (Transaksi)");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2C3E50;");

        // Split Layout: Left (Menu) | Right (Cart)
        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        // --- Left Panel: Menu List ---
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(500);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label menuTitle = new Label("Daftar Menu Tersedia");
        menuTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        // Search Bar
        txtSearch = new TextField();
        txtSearch.setPromptText("Cari Menu...");
        txtSearch.setPrefHeight(35);
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredMenuList != null) {
                filteredMenuList.setPredicate(menu -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    return menu.getNama().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });

        tableMenu = new TableView<>();
        
        TableColumn<MenuRestoran, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(40);

        TableColumn<MenuRestoran, String> colNama = new TableColumn<>("Nama");
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colNama.setPrefWidth(150);

        TableColumn<MenuRestoran, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colHarga.setPrefWidth(100);

        TableColumn<MenuRestoran, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colStok.setPrefWidth(60);

        TableColumn<MenuRestoran, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        colKat.setPrefWidth(100);

        tableMenu.getColumns().addAll(colId, colNama, colHarga, colStok, colKat);
        tableMenu.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableMenu, Priority.ALWAYS);

        Button btnAddCart = new Button("Tambah ke Keranjang");
        btnAddCart.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        btnAddCart.setMaxWidth(Double.MAX_VALUE);
        btnAddCart.setOnAction(e -> handleAddToCart());

        leftPanel.getChildren().addAll(menuTitle, txtSearch, tableMenu, btnAddCart);


        // --- Right Panel: Cart ---
        VBox rightPanel = new VBox(10);
        rightPanel.setPrefWidth(400);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        rightPanel.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label cartTitle = new Label("Keranjang Pesanan");
        cartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        tableCart = new TableView<>();
        
        TableColumn<CartItem, String> cartNama = new TableColumn<>("Nama Menu");
        cartNama.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        
        TableColumn<CartItem, Integer> cartQty = new TableColumn<>("Jumlah");
        cartQty.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        
        TableColumn<CartItem, Double> cartHarga = new TableColumn<>("Harga Satuan");
        cartHarga.setCellValueFactory(new PropertyValueFactory<>("hargaSatuan"));

        TableColumn<CartItem, Double> cartSub = new TableColumn<>("Subtotal");
        cartSub.setCellValueFactory(new PropertyValueFactory<>("subtotal"));

        TableColumn<CartItem, String> cartCatatan = new TableColumn<>("Catatan");
        cartCatatan.setCellValueFactory(new PropertyValueFactory<>("catatan"));
        cartCatatan.setPrefWidth(100);

        tableCart.getColumns().addAll(cartNama, cartQty, cartHarga, cartSub, cartCatatan);
        tableCart.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCart.setItems(cartList);
        VBox.setVgrow(tableCart, Priority.ALWAYS);

        HBox totalBox = new HBox();
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        totalBox.setPadding(new Insets(10, 0, 10, 0));
        lblTotal = new Label("Total: Rp 0.0");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTotal.setStyle("-fx-text-fill: #E74C3C;");
        totalBox.getChildren().add(lblTotal);

        HBox actionCartBox = new HBox(10);
        actionCartBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnHapusCart = new Button("Hapus Item");
        btnHapusCart.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white; -fx-font-weight: bold;");
        btnHapusCart.setOnAction(e -> handleRemoveFromCart());

        Button btnBayar = new Button("BAYAR SEKARANG");
        btnBayar.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-font-size: 16px;");
        btnBayar.setOnAction(e -> handlePayment());

        actionCartBox.getChildren().addAll(btnHapusCart, btnBayar);

        rightPanel.getChildren().addAll(cartTitle, tableCart, totalBox, actionCartBox);

        // Add to Main Layout
        mainLayout.getChildren().addAll(leftPanel, rightPanel);
        
        view.getChildren().addAll(titleLabel, mainLayout);
    }

    private void loadMenuData() {
        menuList = FXCollections.observableArrayList(controller.getAvailableFoods());
        filteredMenuList = new FilteredList<>(menuList, p -> true);
        tableMenu.setItems(filteredMenuList);
    }

    private void handleAddToCart() {
        MenuRestoran selectedMenu = tableMenu.getSelectionModel().getSelectedItem();
        if (selectedMenu == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih menu dari tabel sebelah kiri terlebih dahulu.");
            return;
        }

        // Custom Dialog for Qty and Note
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Tambah ke Keranjang");
        dialog.setHeaderText("Pesanan: " + selectedMenu.getNama());

        ButtonType loginButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtQty = new TextField();
        txtQty.setPromptText("Jumlah (Qty)");
        txtQty.setText("1");
        TextField txtNote = new TextField();
        txtNote.setPromptText("Catatan (Misal: pedas, level 5)");

        grid.add(new Label("Jumlah:"), 0, 0);
        grid.add(txtQty, 1, 0);
        grid.add(new Label("Catatan:"), 0, 1);
        grid.add(txtNote, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(txtQty.getText(), txtNote.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            try {
                int qty = Integer.parseInt(pair.getKey());
                String catatan = pair.getValue();

                if (qty <= 0) throw new NumberFormatException();

                // Cek total stok
                int currentTotalQtyInCart = 0;
                CartItem existingItemWithSameNote = null;
                for (CartItem item : cartList) {
                    if (item.getMenu().getId() == selectedMenu.getId()) {
                        currentTotalQtyInCart += item.getJumlah();
                        if (item.getCatatan().equals(catatan)) {
                            existingItemWithSameNote = item;
                        }
                    }
                }

                if (currentTotalQtyInCart + qty > selectedMenu.getStok()) {
                    showAlert(Alert.AlertType.ERROR, "Stok Tidak Cukup", "Sisa stok untuk " + selectedMenu.getNama() + " hanya " + selectedMenu.getStok() + ".");
                    return;
                }

                if (existingItemWithSameNote != null) {
                    existingItemWithSameNote.setJumlah(existingItemWithSameNote.getJumlah() + qty);
                    tableCart.refresh();
                } else {
                    CartItem newItem = new CartItem(selectedMenu, qty);
                    newItem.setCatatan(catatan);
                    cartList.add(newItem);
                }
                updateTotal();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Tidak Valid", "Jumlah harus berupa angka lebih dari 0.");
            }
        });
    }

    private void handleRemoveFromCart() {
        CartItem selectedItem = tableCart.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cartList.remove(selectedItem);
            updateTotal();
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih item di keranjang yang ingin dihapus.");
        }
    }

    private void updateTotal() {
        totalHarga = 0;
        for (CartItem item : cartList) {
            totalHarga += item.getSubtotal();
        }
        lblTotal.setText("Total: Rp " + String.format("%.2f", totalHarga));
    }

    private void handlePayment() {
        if (cartList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Keranjang Kosong", "Tambahkan minimal satu menu ke keranjang sebelum membayar.");
            return;
        }

        // Konfirmasi Pembayaran
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Konfirmasi Pembayaran");
        confirm.setHeaderText(null);
        confirm.setContentText("Apakah Anda yakin ingin menyelesaikan pembayaran?");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = controller.processPayment(cartList, totalHarga);
            if (success) {
                // Cetak Struk ke file
                cetakStruk(new ArrayList<>(cartList), totalHarga);

                showAlert(Alert.AlertType.INFORMATION, "Pembayaran Berhasil", "Transaksi berhasil disimpan dan stok telah diperbarui.");
                cartList.clear();
                updateTotal();
                loadMenuData(); // Reload menu to get updated stock
            } else {
                showAlert(Alert.AlertType.ERROR, "Pembayaran Gagal", "Terjadi kesalahan saat memproses transaksi.");
            }
        }
    }

    private void cetakStruk(List<CartItem> cart, double total) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "struk_" + timestamp + ".txt";
        
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("========================================\n");
            writer.write("           RESTOMATE RECEIPT            \n");
            writer.write("========================================\n");
            writer.write("Waktu: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("----------------------------------------\n");
            
            for (CartItem item : cart) {
                writer.write(String.format("%-20s x%-3d Rp %10.2f\n", 
                    item.getNamaMenu(), item.getJumlah(), item.getSubtotal()));
                if (item.getCatatan() != null && !item.getCatatan().trim().isEmpty()) {
                    writer.write("  Catatan: " + item.getCatatan() + "\n");
                }
            }
            
            writer.write("----------------------------------------\n");
            writer.write(String.format("TOTAL:                   Rp %10.2f\n", total));
            writer.write("========================================\n");
            writer.write("       Terima kasih atas kunjungan Anda! \n");
            writer.write("========================================\n");
            
        } catch (IOException e) {
            System.err.println("Gagal mencetak struk: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getView() {
        return view;
    }
}