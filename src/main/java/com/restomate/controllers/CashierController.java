package com.restomate.controllers;

import com.restomate.dao.MenuDAO;
import com.restomate.dao.TransactionDAO;
import com.restomate.models.Makanan;
import com.restomate.models.MenuRestoran;
import com.restomate.models.Minuman;
import com.restomate.models.Transaction;
import com.restomate.models.TransactionItem;
import com.restomate.views.CashierView;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.effect.DropShadow;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CashierController {
    private CashierView view;
    private MenuDAO menuDAO;
    private TransactionDAO transactionDAO;
    private ObservableList<CartItem> cartItems; 
    private ObservableList<MenuRestoran> menuItems;
    private List<HoldSession> holdSessions;
    private double currentGrandTotal = 0;

    public CashierController(CashierView view) {
        this.view = view;
        this.menuDAO = new MenuDAO();
        this.transactionDAO = new TransactionDAO();
        this.cartItems = FXCollections.observableArrayList();
        this.menuItems = FXCollections.observableArrayList();
        this.holdSessions = new ArrayList<>();
        
        setupCartTable();
        setupMenuTable();
        setupSearchAndFilter();
        setupListeners();
        
        loadMenus();
        setupActions();
        generateNextQueueNumber();
    }

    private void setupCartTable() {
        TableView<CartItem> table = view.getCartTable();
        
        TableColumn<CartItem, String> colName = new TableColumn<>("Item");
        colName.setCellValueFactory(data -> data.getValue().namaProperty());
        colName.setPrefWidth(130);
        
        TableColumn<CartItem, CartItem> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colQty.setCellFactory(column -> new TableCell<CartItem, CartItem>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Label lblQty = new Label();
            private final HBox container = new HBox(6, btnMinus, lblQty, btnPlus);
            
            {
                container.setAlignment(Pos.CENTER);
                btnMinus.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6; -fx-font-size: 10px;");
                btnMinus.setOnMouseEntered(e -> btnMinus.setStyle("-fx-background-color: #BDBDBD; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6; -fx-font-size: 10px; -fx-cursor: hand;"));
                btnMinus.setOnMouseExited(e -> btnMinus.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 6; -fx-font-size: 10px;"));

                btnPlus.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 5; -fx-font-size: 10px;");
                btnPlus.setOnMouseEntered(e -> btnPlus.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 5; -fx-font-size: 10px; -fx-cursor: hand;"));
                btnPlus.setOnMouseExited(e -> btnPlus.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4; -fx-padding: 2 5; -fx-font-size: 10px;"));
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblQty.setText(String.valueOf(item.getQty()));
                    
                    btnMinus.setOnAction(e -> {
                        if (item.getQty() > 1) {
                            item.setQty(item.getQty() - 1);
                            item.setSubtotal(item.getQty() * item.getHargaSatuan());
                            updateTotal();
                            view.getCartTable().refresh();
                        } else {
                            cartItems.remove(item);
                            updateTotal();
                        }
                    });
                    
                    btnPlus.setOnAction(e -> {
                        int stokLimit = getMenuStock(item.getMenuId());
                        if (item.getQty() < stokLimit) {
                            item.setQty(item.getQty() + 1);
                            item.setSubtotal(item.getQty() * item.getHargaSatuan());
                            updateTotal();
                            view.getCartTable().refresh();
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Stok Habis", "Udah gak bisa nambah lagi, stok tinggal " + stokLimit);
                        }
                    });
                    
                    setGraphic(container);
                }
            }
        });
        colQty.setPrefWidth(95);
        colQty.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<CartItem, String> colSubtotal = new TableColumn<>("Subtotal");
        colSubtotal.setCellValueFactory(data -> {
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            return new SimpleStringProperty("Rp " + formatter.format(data.getValue().getSubtotal()));
        });
        colSubtotal.setPrefWidth(90);

        TableColumn<CartItem, CartItem> colAksi = new TableColumn<>("Aksi");
        colAksi.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colAksi.setCellFactory(column -> new TableCell<CartItem, CartItem>() {
            private final Button btnDel = new Button("🗑️");
            {
                btnDel.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8;");
                btnDel.setOnMouseEntered(e -> btnDel.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8; -fx-cursor: hand;"));
                btnDel.setOnMouseExited(e -> btnDel.setStyle("-fx-background-color: #FFCDD2; -fx-text-fill: #D32F2F; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 3 8;"));
            }
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    btnDel.setOnAction(e -> {
                        cartItems.remove(item);
                        updateTotal();
                    });
                    setGraphic(btnDel);
                }
            }
        });
        colAksi.setPrefWidth(40);
        colAksi.setStyle("-fx-alignment: CENTER;");
        
        table.getColumns().addAll(colName, colQty, colSubtotal, colAksi);
        table.setItems(cartItems);
    }

    private void setupListeners() {
        // Listener Uang Dibayar
        view.getTxtAmountPaid().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getTxtAmountPaid().setText(newVal.replaceAll("[^\\d]", ""));
                return;
            }
            updateTotal();
        });
        
        // Listener Diskon
        view.getCmbDiscount().setOnAction(e -> updateTotal());
        
        // Listener Metode Pembayaran
        view.getCmbPayment().setOnAction(e -> {
            updateTotal();
            boolean isQris = "QRIS".equalsIgnoreCase(view.getCmbPayment().getValue());
            view.getBtnPas().setDisable(isQris);
            view.getBtn10k().setDisable(isQris);
            view.getBtn20k().setDisable(isQris);
            view.getBtn50k().setDisable(isQris);
            view.getBtn100k().setDisable(isQris);
        });
    }
    
    // Fitur pencarian otomatis nge-refresh kartu tiap kali kita ngetik
    private void setupSearchAndFilter() {
        // Setiap kali ada ketikan baru di search bar, langsung render ulang!
        view.getTxtSearch().textProperty().addListener((obs, oldVal, newVal) -> {
            loadMenus();
        });
        
        // Setiap kali ComboBox filter diganti (Makanan/Minuman), render ulang juga!
        view.getCmbFilter().setOnAction(e -> {
            loadMenus();
        });
    }

    public void refresh() {
        loadMenus();
        if (cartItems.isEmpty()) {
            generateNextQueueNumber();
        }
    }

    private void generateNextQueueNumber() {
        try {
            int countToday = transactionDAO.getTodayTransactionCount();
            int nextQueue = countToday + 1;
            view.getTxtNomorAntrian().setText(String.valueOf(nextQueue));
        } catch (Exception e) {
            view.getTxtNomorAntrian().setText("1");
        }
    }

    private int getMenuStock(int menuId) {
        List<MenuRestoran> menus = menuDAO.getAllMenus();
        for (MenuRestoran m : menus) {
            if (m.getId() == menuId) {
                return m.getStok();
            }
        }
        return 0;
    }

    private void setupMenuTable() {
        TableView<MenuRestoran> table = view.getMenuTable();

        // 1. Column Nama Menu
        TableColumn<MenuRestoran, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNama()));
        colNama.setPrefWidth(180);

        // 2. Column Kategori
        TableColumn<MenuRestoran, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKategori()));
        colKategori.setPrefWidth(100);

        // 3. Column Harga
        TableColumn<MenuRestoran, String> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(data -> {
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            return new SimpleStringProperty("Rp " + formatter.format(data.getValue().getHarga()));
        });
        colHarga.setPrefWidth(120);

        // 4. Column Stok
        TableColumn<MenuRestoran, String> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStok())));
        colStok.setPrefWidth(70);
        colStok.setStyle("-fx-alignment: CENTER;");

        // 5. Column Keterangan
        TableColumn<MenuRestoran, String> colDetail = new TableColumn<>("Keterangan");
        colDetail.setCellValueFactory(data -> {
            MenuRestoran menu = data.getValue();
            if (menu instanceof Makanan) {
                return new SimpleStringProperty("🌶️ Pedas: " + (((Makanan) menu).getTingkatPedas() != null ? ((Makanan) menu).getTingkatPedas() : "-"));
            } else if (menu instanceof Minuman) {
                return new SimpleStringProperty(((Minuman) menu).isDingin() ? "❄️ Dingin" : "🔥 Panas");
            }
            return new SimpleStringProperty("-");
        });
        colDetail.setPrefWidth(120);

        // 6. Column Aksi
        TableColumn<MenuRestoran, MenuRestoran> colAksi = new TableColumn<>("Aksi");
        colAksi.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        colAksi.setCellFactory(column -> new TableCell<MenuRestoran, MenuRestoran>() {
            private final Button btnAdd = new Button("+ Keranjang");
            {
                btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnAdd.setOnMouseEntered(e -> btnAdd.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;"));
                btnAdd.setOnMouseExited(e -> btnAdd.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
            }
            @Override
            protected void updateItem(MenuRestoran item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    btnAdd.setOnAction(e -> addToCart(item));
                    setGraphic(btnAdd);
                }
            }
        });
        colAksi.setPrefWidth(120);
        colAksi.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(colNama, colKategori, colHarga, colStok, colDetail, colAksi);
        table.setItems(menuItems);

        // Shortcut double click pada baris tabel menu
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MenuRestoran selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    addToCart(selected);
                }
            }
        });
    }

    public void loadMenus() {
        List<MenuRestoran> menus = menuDAO.getAllMenus();
        menuItems.clear();
        
        if (menus == null || menus.isEmpty()) {
            view.getMenuTable().setPlaceholder(new Label("Yah, belum ada menu nih di database. Tambahin lewat layar Kelola Menu dulu ya!"));
            return;
        }
        
        String searchQuery = view.getTxtSearch().getText().toLowerCase();
        String filterCat = view.getCmbFilter().getValue();
        
        for (MenuRestoran menu : menus) {
            // Syarat 1: Stok masih ada
            if (menu.getStok() <= 0) continue;
            
            // Syarat 2: Filter Kategori
            if (!"SEMUA".equals(filterCat) && !menu.getKategori().equalsIgnoreCase(filterCat)) {
                continue;
            }
            
            // Syarat 3: Filter Pencarian Nama
            if (!searchQuery.isEmpty() && !menu.getNama().toLowerCase().contains(searchQuery)) {
                continue;
            }
            
            menuItems.add(menu);
        }
        
        if (menuItems.isEmpty()) {
            view.getMenuTable().setPlaceholder(new Label("Gak nemu menu yang dicari nih. Coba kata kunci atau kategori lain."));
        }
    }

    private void addToCart(MenuRestoran menu) {
        for (CartItem item : cartItems) {
            if (item.getMenuId() == menu.getId()) {
                if (item.getQty() < menu.getStok()) {
                    item.setQty(item.getQty() + 1);
                    item.setSubtotal(item.getQty() * menu.getHarga());
                    updateTotal();
                    view.getCartTable().refresh(); 
                } else {
                    showAlert(Alert.AlertType.WARNING, "Stok Habis", "Udah gak bisa nambah lagi, stok tinggal " + menu.getStok());
                }
                return;
            }
        }
        
        cartItems.add(new CartItem(menu.getId(), menu.getNama(), 1, menu.getHarga()));
        updateTotal();
    }

    private void updateTotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        
        // Diskon
        String discStr = view.getCmbDiscount().getValue();
        double discPercent = 0;
        if (discStr != null) {
            discPercent = Double.parseDouble(discStr.replace("%", ""));
        }
        double discountAmount = subtotal * (discPercent / 100);
        
        // Pajak PB1 (10% dari nominal kena pajak)
        double taxableAmount = subtotal - discountAmount;
        double taxAmount = taxableAmount * 0.10;
        
        // Grand Total
        double grandTotal = taxableAmount + taxAmount;
        this.currentGrandTotal = grandTotal;
        
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        
        view.getLblSubtotal().setText("Rp " + formatter.format(subtotal));
        view.getLblDiscountAmount().setText("Rp " + formatter.format(discountAmount));
        view.getLblTaxAmount().setText("Rp " + formatter.format(taxAmount));
        view.getLblTotal().setText("Rp " + formatter.format(grandTotal));
        
        updateChange(grandTotal);
    }

    private void updateChange(double grandTotal) {
        String payment = view.getCmbPayment().getValue();
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        
        if ("QRIS".equalsIgnoreCase(payment)) {
            view.getTxtAmountPaid().setText(String.valueOf((int) grandTotal));
            view.getTxtAmountPaid().setDisable(true);
            view.getLblChange().setText("Rp 0");
            view.getLblChange().setTextFill(Color.web("#4CAF50")); // Green
        } else {
            view.getTxtAmountPaid().setDisable(false);
            String paidText = view.getTxtAmountPaid().getText().trim();
            if (paidText.isEmpty()) {
                view.getLblChange().setText("Rp 0");
                view.getLblChange().setTextFill(Color.GRAY);
                return;
            }
            try {
                double paidAmount = Double.parseDouble(paidText);
                double change = paidAmount - grandTotal;
                if (change >= 0) {
                    view.getLblChange().setText("Rp " + formatter.format(change));
                    view.getLblChange().setTextFill(Color.web("#4CAF50")); // Green
                } else {
                    view.getLblChange().setText("Kurang: Rp " + formatter.format(Math.abs(change)));
                    view.getLblChange().setTextFill(Color.web("#D32F2F")); // Red
                }
            } catch (NumberFormatException ex) {
                view.getLblChange().setText("Format salah");
                view.getLblChange().setTextFill(Color.web("#D32F2F"));
            }
        }
    }

    private void setupActions() {
        // Logika Bersihkan Keranjang
        view.getBtnClearCart().setOnAction(e -> {
            cartItems.clear();
            view.getTxtCatatan().clear();
            view.getTxtAmountPaid().clear();
            view.getTxtNamaPelanggan().clear();
            generateNextQueueNumber();
            view.getCmbOrderType().setValue("DINE IN");
            view.getCmbDiscount().setValue("0%");
            view.getCmbPayment().setValue("CASH");
            updateTotal();
        });

        // Quick Cash Buttons
        view.getBtnPas().setOnAction(e -> handleQuickCash(-1));
        view.getBtn10k().setOnAction(e -> handleQuickCash(10000));
        view.getBtn20k().setOnAction(e -> handleQuickCash(20000));
        view.getBtn50k().setOnAction(e -> handleQuickCash(50000));
        view.getBtn100k().setOnAction(e -> handleQuickCash(100000));

        // Hold & Recall
        view.getBtnHold().setOnAction(e -> handleHold());
        view.getBtnRecall().setOnAction(e -> handleRecall());
        
        // Logika Bayar
        view.getBtnPay().setOnAction(e -> {
            if (cartItems.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Keranjang Kosong", "Pilih menu dulu dong sebelum bayar.");
                return;
            }
            
            double subtotal = 0;
            List<TransactionItem> dbItems = new ArrayList<>();
            for (CartItem ci : cartItems) {
                subtotal += ci.getSubtotal();
                TransactionItem ti = new TransactionItem(0, 0, ci.getMenuId(), ci.getQty(), ci.getSubtotal());
                dbItems.add(ti);
            }
            
            // Re-calculate values for DB and Receipt
            String discStr = view.getCmbDiscount().getValue();
            double discPercent = 0;
            if (discStr != null) {
                discPercent = Double.parseDouble(discStr.replace("%", ""));
            }
            double discountAmount = subtotal * (discPercent / 100);
            double taxableAmount = subtotal - discountAmount;
            double taxAmount = taxableAmount * 0.10;
            double grandTotal = taxableAmount + taxAmount;
            
            String paidText = view.getTxtAmountPaid().getText().trim();
            double paidAmount = grandTotal;
            if (!"QRIS".equalsIgnoreCase(view.getCmbPayment().getValue())) {
                if (paidText.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Uang Belum Diinput", "Masukkan nominal uang dibayar pelanggan terlebih dahulu!");
                    return;
                }
                try {
                    paidAmount = Double.parseDouble(paidText);
                    if (paidAmount < grandTotal) {
                        showAlert(Alert.AlertType.WARNING, "Uang Kurang", "Nominal uang dibayar kurang dari total tagihan!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, "Format Uang Salah", "Nominal uang dibayar harus berupa angka!");
                    return;
                }
            }
            double changeAmount = paidAmount - grandTotal;
            
            // Format notes/catatan untuk database
            StringBuilder notesBuilder = new StringBuilder();
            if (discPercent > 0) {
                notesBuilder.append("[Diskon: ").append(discStr).append("] ");
            }
            notesBuilder.append("[Pajak PB1: 10%] ");
            String userNote = view.getTxtCatatan().getText().trim();
            if (!userNote.isEmpty()) {
                notesBuilder.append("Catatan: ").append(userNote);
            }
            
            Transaction tx = new Transaction(0, grandTotal, view.getCmbPayment().getValue(), notesBuilder.toString(), LocalDateTime.now());
            tx.setNamaPelanggan(view.getTxtNamaPelanggan().getText().trim());
            tx.setNomorAntrian(view.getTxtNomorAntrian().getText().trim());
            tx.setTipePesanan(view.getCmbOrderType().getValue());
            
            boolean success = transactionDAO.saveTransaction(tx, dbItems);
            if (success) {
                // Generate Receipt String
                String receiptText = generateReceiptText(subtotal, discPercent, discountAmount, taxAmount, grandTotal, paidAmount, changeAmount, userNote);
                
                // Save receipt to a text file
                try {
                    java.nio.file.Path receiptsDir = java.nio.file.Paths.get("receipts");
                    if (!java.nio.file.Files.exists(receiptsDir)) {
                        java.nio.file.Files.createDirectories(receiptsDir);
                    }
                    String filename = "receipt_" + System.currentTimeMillis() + ".txt";
                    java.nio.file.Files.writeString(receiptsDir.resolve(filename), receiptText);
                } catch (Exception ex) {
                    System.err.println("Gagal mencetak struk ke file: " + ex.getMessage());
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Pembayaran berhasil disimpan. Struk telah dicetak.");
                cartItems.clear();
                view.getTxtSearch().clear();
                view.getCmbFilter().setValue("SEMUA");
                view.getTxtCatatan().clear();
                view.getTxtAmountPaid().clear();
                view.getTxtNamaPelanggan().clear();
                generateNextQueueNumber();
                view.getCmbOrderType().setValue("DINE IN");
                view.getCmbDiscount().setValue("0%");
                view.getCmbPayment().setValue("CASH");
                updateTotal();
                loadMenus(); 
                
                // Show receipt pop-up dialog
                showReceiptDialog(receiptText);
            } else {
                showAlert(Alert.AlertType.ERROR, "Waduh Error", "Gagal menyimpan transaksi ke database.");
            }
        });

        // Setup shortcut keyboard
        view.getView().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    switch (event.getCode()) {
                        case F3:
                            view.getTxtSearch().requestFocus();
                            event.consume();
                            break;
                        case F5:
                            view.getBtnPay().fire();
                            event.consume();
                            break;
                        case ESCAPE:
                            view.getBtnClearCart().fire();
                            event.consume();
                            break;
                        default:
                            break;
                    }
                });
            }
        });
    }

    private String generateReceiptText(double subtotal, double discPercent, double discountAmount, double taxAmount, double grandTotal, double paidAmount, double changeAmount, String userNote) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        
        String namaPelanggan = view.getTxtNamaPelanggan().getText().trim();
        String nomorAntrian = view.getTxtNomorAntrian().getText().trim();
        String tipePesanan = view.getCmbOrderType().getValue();

        StringBuilder sb = new StringBuilder();
        sb.append("================================\n");
        sb.append("           RESTOMATE            \n");
        sb.append("================================\n");
        sb.append("Tanggal : ").append(LocalDateTime.now().format(dtf)).append("\n");
        sb.append("Kasir   : admin\n");
        if (!namaPelanggan.isEmpty()) {
            sb.append("Plg     : ").append(namaPelanggan).append("\n");
        }
        if (!nomorAntrian.isEmpty()) {
            sb.append("Antrian : ").append(nomorAntrian).append("\n");
        }
        sb.append("Tipe    : ").append(tipePesanan).append("\n");
        sb.append("--------------------------------\n");
        
        for (CartItem ci : cartItems) {
            String nameLine = ci.namaProperty().get();
            if (nameLine.length() > 18) {
                nameLine = nameLine.substring(0, 15) + "...";
            }
            String qtyPrice = ci.getQty() + "x";
            String subtotalStr = formatter.format(ci.getSubtotal());
            
            int spaces = 32 - nameLine.length() - subtotalStr.length();
            if (spaces < 1) spaces = 1;
            sb.append(nameLine).append(" ".repeat(spaces)).append(subtotalStr).append("\n");
            sb.append("  ").append(qtyPrice).append("\n");
        }
        sb.append("--------------------------------\n");
        
        String subtotalStr = "Rp " + formatter.format(subtotal);
        sb.append("Subtotal:").append(" ".repeat(32 - 9 - subtotalStr.length())).append(subtotalStr).append("\n");
        
        if (discPercent > 0) {
            String discStr = "-Rp " + formatter.format(discountAmount);
            String discLabel = "Diskon (" + (int) discPercent + "%):";
            sb.append(discLabel).append(" ".repeat(32 - discLabel.length() - discStr.length())).append(discStr).append("\n");
        }
        
        String taxStr = "Rp " + formatter.format(taxAmount);
        sb.append("Pajak PB1 (10%):").append(" ".repeat(32 - 16 - taxStr.length())).append(taxStr).append("\n");
        sb.append("--------------------------------\n");
        
        String grandTotalStr = "Rp " + formatter.format(grandTotal);
        sb.append("TOTAL:").append(" ".repeat(32 - 6 - grandTotalStr.length())).append(grandTotalStr).append("\n");
        
        String method = view.getCmbPayment().getValue();
        sb.append("Metode:").append(" ".repeat(32 - 7 - method.length())).append(method).append("\n");
        
        String paidStr = "Rp " + formatter.format(paidAmount);
        sb.append("Dibayar:").append(" ".repeat(32 - 8 - paidStr.length())).append(paidStr).append("\n");
        
        String changeStr = "Rp " + formatter.format(changeAmount);
        sb.append("Kembalian:").append(" ".repeat(32 - 10 - changeStr.length())).append(changeStr).append("\n");
        
        if (userNote != null && !userNote.trim().isEmpty()) {
            sb.append("--------------------------------\n");
            sb.append("Catatan: ").append(userNote).append("\n");
        }
        sb.append("================================\n");
        sb.append("Terima kasih atas kunjungan Anda\n");
        sb.append("================================\n");
        
        return sb.toString();
    }

    private void showReceiptDialog(String receiptText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Struk Pembayaran");
        alert.setHeaderText("Transaksi Berhasil & Struk Dicetak!");
        
        TextArea textArea = new TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setFont(Font.font("Consolas", 12));
        textArea.setPrefSize(350, 450);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void handleQuickCash(double amount) {
        if ("QRIS".equalsIgnoreCase(view.getCmbPayment().getValue())) {
            return;
        }
        if (amount == -1) {
            view.getTxtAmountPaid().setText(String.valueOf((int) Math.ceil(currentGrandTotal)));
        } else {
            view.getTxtAmountPaid().setText(String.valueOf((int) amount));
        }
        updateTotal();
    }

    private void handleHold() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Tangguhkan Pesanan", "Keranjang kosong! Tidak ada pesanan untuk ditangguhkan.");
            return;
        }
        
        HoldSession session = new HoldSession(
            view.getTxtNamaPelanggan().getText().trim(),
            view.getTxtNomorAntrian().getText().trim(),
            view.getCmbOrderType().getValue(),
            view.getCmbDiscount().getValue(),
            view.getTxtCatatan().getText().trim(),
            view.getCmbPayment().getValue(),
            view.getTxtAmountPaid().getText().trim(),
            cartItems
        );
        
        holdSessions.add(session);
        updateRecallCount();
        
        // Reset Cashier screen state for next customer
        cartItems.clear();
        view.getTxtCatatan().clear();
        view.getTxtAmountPaid().clear();
        view.getTxtNamaPelanggan().clear();
        generateNextQueueNumber();
        view.getCmbOrderType().setValue("DINE IN");
        view.getCmbDiscount().setValue("0%");
        view.getCmbPayment().setValue("CASH");
        updateTotal();
        
        showAlert(Alert.AlertType.INFORMATION, "Sukses", "Pesanan berhasil ditangguhkan.");
    }

    private void handleRecall() {
        if (holdSessions.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Recall Pesanan", "Tidak ada pesanan yang ditangguhkan saat ini.");
            return;
        }

        Dialog<HoldSession> dialog = new Dialog<>();
        dialog.setTitle("Recall Pesanan");
        dialog.setHeaderText("Pilih pesanan yang ingin dimuat kembali:");

        ButtonType btnSelect = new ButtonType("Pilih", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSelect, btnCancel);

        ListView<HoldSession> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(holdSessions));
        listView.setCellFactory(param -> new ListCell<HoldSession>() {
            @Override
            protected void updateItem(HoldSession session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                } else {
                    java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
                    double total = 0;
                    for (CartItem ci : session.getCartItems()) {
                        total += ci.getSubtotal();
                    }
                    String timeStr = session.getTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    String plg = session.getCustomerName().isEmpty() ? "-" : session.getCustomerName();
                    String antrian = session.getQueueNumber().isEmpty() ? "-" : session.getQueueNumber();
                    setText(String.format("[%s] Antrian: %s | Pelanggan: %s | Total: Rp %s (%s)", 
                        timeStr, antrian, plg, formatter.format(total), session.getOrderType()));
                }
            }
        });
        
        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSelect) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(selectedSession -> {
            cartItems.setAll(selectedSession.getCartItems());
            view.getTxtNamaPelanggan().setText(selectedSession.getCustomerName());
            view.getTxtNomorAntrian().setText(selectedSession.getQueueNumber());
            view.getCmbOrderType().setValue(selectedSession.getOrderType());
            view.getCmbDiscount().setValue(selectedSession.getDiscount());
            view.getTxtCatatan().setText(selectedSession.getNotes());
            view.getCmbPayment().setValue(selectedSession.getPaymentMethod());
            view.getTxtAmountPaid().setText(selectedSession.getAmountPaid());
            
            holdSessions.remove(selectedSession);
            updateTotal();
            updateRecallCount();
        });
    }

    private void updateRecallCount() {
        view.getBtnRecall().setText("📤 Recall (" + holdSessions.size() + ")");
    }

    // Kelas dummy untuk di-bind ke JavaFX TableView
    public static class CartItem {
        private final SimpleIntegerProperty menuId;
        private final SimpleStringProperty nama;
        private final SimpleIntegerProperty qty;
        private final SimpleDoubleProperty subtotal;
        private final double hargaSatuan;

        public CartItem(int menuId, String nama, int qty, double subtotal) {
            this.menuId = new SimpleIntegerProperty(menuId);
            this.nama = new SimpleStringProperty(nama);
            this.qty = new SimpleIntegerProperty(qty);
            this.subtotal = new SimpleDoubleProperty(subtotal);
            this.hargaSatuan = subtotal / qty;
        }

        public int getMenuId() { return menuId.get(); }
        public int getQty() { return qty.get(); }
        public void setQty(int value) { qty.set(value); }
        public double getSubtotal() { return subtotal.get(); }
        public void setSubtotal(double value) { subtotal.set(value); }
        public double getHargaSatuan() { return hargaSatuan; }
        
        public SimpleStringProperty namaProperty() { return nama; }
        public SimpleIntegerProperty qtyProperty() { return qty; }
        public SimpleDoubleProperty subtotalProperty() { return subtotal; }
    }

    public static class HoldSession {
        private final String customerName;
        private final String queueNumber;
        private final String orderType;
        private final String discount;
        private final String notes;
        private final String paymentMethod;
        private final String amountPaid;
        private final List<CartItem> cartItems;
        private final LocalDateTime time;

        public HoldSession(String customerName, String queueNumber, String orderType, String discount, String notes, String paymentMethod, String amountPaid, List<CartItem> cartItems) {
            this.customerName = customerName;
            this.queueNumber = queueNumber;
            this.orderType = orderType;
            this.discount = discount;
            this.notes = notes;
            this.paymentMethod = paymentMethod;
            this.amountPaid = amountPaid;
            this.cartItems = new ArrayList<>(cartItems);
            this.time = LocalDateTime.now();
        }

        public String getCustomerName() { return customerName; }
        public String getQueueNumber() { return queueNumber; }
        public String getOrderType() { return orderType; }
        public String getDiscount() { return discount; }
        public String getNotes() { return notes; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getAmountPaid() { return amountPaid; }
        public List<CartItem> getCartItems() { return cartItems; }
        public LocalDateTime getTime() { return time; }
    }
}
