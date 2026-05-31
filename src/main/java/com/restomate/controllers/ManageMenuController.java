package com.restomate.controllers;

import com.restomate.dao.MenuDAO;
import com.restomate.models.Makanan;
import com.restomate.models.MenuRestoran;
import com.restomate.models.Minuman;
import com.restomate.views.ManageMenuView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import javafx.stage.FileChooser;
import javafx.scene.control.TableRow;

public class ManageMenuController {

    private ManageMenuView view;
    private MenuDAO menuDAO;
    private ObservableList<MenuRestoran> menuList;
    
    // Simpan semua data ori di sini biar gampang difilter
    private List<MenuRestoran> allMenus;

    public ManageMenuController(ManageMenuView view) {
        this.view = view;
        this.menuDAO = new MenuDAO();
        this.menuList = FXCollections.observableArrayList();
        
        setupTable();
        setupSearchAndFilter();
        setupActions();
        
        loadData();
    }

    private void setupTable() {
        TableView<MenuRestoran> table = view.getTableMenu();
        
        TableColumn<MenuRestoran, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colId.setPrefWidth(50);
        
        TableColumn<MenuRestoran, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNama()));
        colNama.setPrefWidth(200);
        
        TableColumn<MenuRestoran, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKategori()));
        colKat.setPrefWidth(100);
        
        TableColumn<MenuRestoran, String> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(data -> {
            java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("id", "ID"));
            return new SimpleStringProperty("Rp " + formatter.format(data.getValue().getHarga()));
        });
        colHarga.setPrefWidth(120);
        
        TableColumn<MenuRestoran, String> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getStok())));
        colStok.setPrefWidth(70);
        
        TableColumn<MenuRestoran, String> colDetail = new TableColumn<>("Keterangan Tambahan");
        colDetail.setCellValueFactory(data -> {
            MenuRestoran menu = data.getValue();
            if (menu instanceof Makanan) {
                return new SimpleStringProperty("🌶️ Pedas: " + ((Makanan) menu).getTingkatPedas());
            } else if (menu instanceof Minuman) {
                return new SimpleStringProperty(((Minuman) menu).isDingin() ? "❄️ Es/Dingin" : "🔥 Panas");
            }
            return new SimpleStringProperty("-");
        });
        colDetail.setPrefWidth(180);
        
        table.getColumns().addAll(colId, colNama, colKat, colHarga, colStok, colDetail);
        table.setItems(menuList);
        
        // Custom Row Factory for Row Highlighting (Stok Kritis)
        table.setRowFactory(tv -> {
            TableRow<MenuRestoran> row = new TableRow<MenuRestoran>() {
                @Override
                protected void updateItem(MenuRestoran item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setStyle("");
                    } else if (isSelected()) {
                        setStyle(""); // Default selection style
                    } else if (item.getStok() == 0) {
                        setStyle("-fx-background-color: #FFCDD2;"); // light red
                    } else if (item.getStok() < 10) {
                        setStyle("-fx-background-color: #FFF9C4;"); // light yellow
                    } else {
                        setStyle("");
                    }
                }
            };
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                MenuRestoran item = row.getItem();
                if (item != null) {
                    if (newVal) {
                        row.setStyle(""); // Selected
                    } else {
                        if (item.getStok() == 0) {
                            row.setStyle("-fx-background-color: #FFCDD2;");
                        } else if (item.getStok() < 10) {
                            row.setStyle("-fx-background-color: #FFF9C4;");
                        } else {
                            row.setStyle("");
                        }
                    }
                }
            });
            return row;
        });
        
        // Event kalau baris tabel diklik, langsung oper datanya ke form sebelah kanan
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fillForm(newSel);
            }
        });
    }

    private void setupSearchAndFilter() {
        // Nge-filter otomatis pas user ngetik di search bar
        view.getTxtSearch().textProperty().addListener((obs, old, newVal) -> {
            applyFilters();
        });
        
        // Nge-filter pas user ganti dropdown kategori
        view.getCmbFilter().setOnAction(e -> {
            applyFilters();
        });
    }

    public void refresh() {
        loadData();
    }

    private void loadData() {
        // Ambil data seger dari database
        allMenus = menuDAO.getAllMenus();
        applyFilters(); // Langsung terapkan filter yang lagi aktif
        updateStats();
    }
    
    private void updateStats() {
        if (allMenus == null) {
            view.getLblTotalMenu().setText("0");
            view.getLblTotalMakanan().setText("0");
            view.getLblTotalMinuman().setText("0");
            view.getLblTotalKritis().setText("0");
            return;
        }
        
        int total = allMenus.size();
        int makanan = 0;
        int minuman = 0;
        int kritis = 0;
        
        for (MenuRestoran menu : allMenus) {
            if ("MAKANAN".equalsIgnoreCase(menu.getKategori())) {
                makanan++;
            } else if ("MINUMAN".equalsIgnoreCase(menu.getKategori())) {
                minuman++;
            }
            
            if (menu.getStok() < 10) {
                kritis++;
            }
        }
        
        view.getLblTotalMenu().setText(String.valueOf(total));
        view.getLblTotalMakanan().setText(String.valueOf(makanan));
        view.getLblTotalMinuman().setText(String.valueOf(minuman));
        view.getLblTotalKritis().setText(String.valueOf(kritis));
    }
    
    // Fungsi khusus buat nge-filter list
    private void applyFilters() {
        if (allMenus == null) return;
        
        String searchQuery = view.getTxtSearch().getText().toLowerCase();
        String filterCat = view.getCmbFilter().getValue();
        
        menuList.clear();
        for (MenuRestoran menu : allMenus) {
            // Cek Kategori
            if (!"SEMUA".equals(filterCat) && !menu.getKategori().equalsIgnoreCase(filterCat)) {
                continue;
            }
            // Cek Pencarian
            if (!searchQuery.isEmpty() && !menu.getNama().toLowerCase().contains(searchQuery)) {
                continue;
            }
            menuList.add(menu);
        }
    }

    private void fillForm(MenuRestoran menu) {
        view.getTxtId().setText(String.valueOf(menu.getId()));
        view.getTxtNama().setText(menu.getNama());
        view.getTxtHarga().setText(String.valueOf((int)menu.getHarga()));
        view.getTxtStok().setText(String.valueOf(menu.getStok()));
        view.getCmbKategori().setValue(menu.getKategori());
        
        view.getTxtTingkatPedas().setText("");
        view.getChkDingin().setSelected(false);
        

        
        if (menu instanceof Makanan) {
            view.getTxtTingkatPedas().setText(((Makanan) menu).getTingkatPedas());
        } else if (menu instanceof Minuman) {
            view.getChkDingin().setSelected(((Minuman) menu).isDingin());
        }
    }

    private void clearForm() {
        view.getTxtId().clear();
        view.getTxtNama().clear();
        view.getTxtHarga().clear();
        view.getTxtStok().clear();
        view.getTxtTingkatPedas().clear();
        view.getChkDingin().setSelected(false);
        view.getCmbKategori().setValue("MAKANAN"); 
        
        view.getTableMenu().getSelectionModel().clearSelection();
    }

    private void setupActions() {
        // Input Filter hanya Angka
        view.getTxtHarga().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getTxtHarga().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
        view.getTxtStok().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                view.getTxtStok().setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Duplikat Data
        view.getBtnDuplicate().setOnAction(e -> {
            view.getTxtId().clear(); // Kosongkan ID agar dianggap item baru
            view.getTableMenu().getSelectionModel().clearSelection();
            showAlert(Alert.AlertType.INFORMATION, "Siap Duplikat", "ID telah dikosongkan. Silakan edit nama/harga/stok dan klik 'Simpan Data' untuk menyimpan sebagai menu baru.");
        });

        // Ekspor CSV
        view.getBtnExportCsv().setOnAction(e -> {
            if (allMenus == null || allMenus.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Data Kosong", "Tidak ada data menu untuk diekspor!");
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ekspor Data Menu ke CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("menu_restoran.csv");
            
            File file = fileChooser.showSaveDialog(view.getView().getScene().getWindow());
            if (file != null) {
                try (PrintWriter writer = new PrintWriter(file)) {
                    writer.println("nama,harga,kategori,stok,tingkat_pedas,is_dingin");
                    
                    for (MenuRestoran menu : allMenus) {
                        String pedas = "";
                        String dingin = "0";
                        if (menu instanceof Makanan) {
                            pedas = ((Makanan) menu).getTingkatPedas();
                            if (pedas == null) pedas = "";
                        } else if (menu instanceof Minuman) {
                            dingin = ((Minuman) menu).isDingin() ? "1" : "0";
                        }
                        
                        String nama = menu.getNama();
                        if (nama.contains(",")) {
                            nama = "\"" + nama + "\"";
                        }
                        
                        writer.printf("%s,%.0f,%s,%d,%s,%s\n", 
                            nama, menu.getHarga(), menu.getKategori(), menu.getStok(), pedas, dingin);
                    }
                    showAlert(Alert.AlertType.INFORMATION, "Ekspor Sukses", "Data menu berhasil diekspor ke: " + file.getName());
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Ekspor Gagal", "Gagal menulis berkas CSV: " + ex.getMessage());
                }
            }
        });

        // Impor CSV
        view.getBtnImportCsv().setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Impor Data Menu dari CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            
            File file = fileChooser.showOpenDialog(view.getView().getScene().getWindow());
            if (file != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line = reader.readLine(); // Skip header
                    if (line == null) {
                        showAlert(Alert.AlertType.WARNING, "File Kosong", "Berkas CSV tidak memiliki baris data!");
                        return;
                    }
                    
                    List<MenuRestoran> importList = new ArrayList<>();
                    int lineNum = 1;
                    boolean parseError = false;
                    
                    while ((line = reader.readLine()) != null) {
                        lineNum++;
                        if (line.trim().isEmpty()) continue;
                        
                        String[] tokens = parseCsvLine(line);
                        if (tokens.length < 6) {
                            showAlert(Alert.AlertType.ERROR, "Format CSV Salah", 
                                "Gagal membaca baris " + lineNum + ": Jumlah kolom kurang dari 6.");
                            parseError = true;
                            break;
                        }
                        
                        try {
                            String nama = tokens[0].trim();
                            double harga = Double.parseDouble(tokens[1].trim());
                            String kategori = tokens[2].trim().toUpperCase();
                            int stok = Integer.parseInt(tokens[3].trim());
                            String pedas = tokens[4].trim();
                            boolean dingin = "1".equals(tokens[5].trim());
                            
                            if (harga < 0 || stok < 0) {
                                showAlert(Alert.AlertType.ERROR, "Nilai Tidak Valid", 
                                    "Gagal membaca baris " + lineNum + ": Harga atau stok bernilai negatif.");
                                parseError = true;
                                break;
                            }
                            
                            MenuRestoran menu;
                            if ("MAKANAN".equals(kategori)) {
                                menu = new Makanan(0, nama, harga, stok, pedas);
                            } else {
                                menu = new Minuman(0, nama, harga, stok, dingin);
                            }
                            importList.add(menu);
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Parse Error", 
                                "Gagal membaca baris " + lineNum + ": Kesalahan tipe data harga atau stok.");
                            parseError = true;
                            break;
                        }
                    }
                    
                    if (!parseError && !importList.isEmpty()) {
                        boolean success = menuDAO.importMenus(importList);
                        if (success) {
                            loadData(); // Refresh UI & Stats
                            showAlert(Alert.AlertType.INFORMATION, "Impor Sukses", 
                                "Berhasil mengimpor " + importList.size() + " data menu secara aman.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Impor Gagal", "Terjadi kesalahan saat menyimpan data menu ke database.");
                        }
                    } else if (importList.isEmpty() && !parseError) {
                        showAlert(Alert.AlertType.WARNING, "Tidak Ada Data", "Tidak ada data menu baru yang berhasil diimpor.");
                    }
                } catch (IOException ex) {
                    showAlert(Alert.AlertType.ERROR, "Impor Gagal", "Gagal membaca berkas CSV: " + ex.getMessage());
                }
            }
        });

        // Simpan Data (Insert atau Update)
        view.getBtnSave().setOnAction(e -> {
            try {
                String nama = view.getTxtNama().getText();
                String hargaText = view.getTxtHarga().getText();
                String stokText = view.getTxtStok().getText();
                
                if (nama.isEmpty() || hargaText.isEmpty() || stokText.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Isian Belum Lengkap", "Nama, Harga, dan Stok wajib diisi ya bos!");
                    return;
                }
                
                double harga = Double.parseDouble(hargaText);
                int stok = Integer.parseInt(stokText);
                
                if (harga < 0 || stok < 0) {
                    showAlert(Alert.AlertType.WARNING, "Nilai Tidak Valid", "Harga dan stok tidak boleh bernilai negatif!");
                    return;
                }
                String kategori = view.getCmbKategori().getValue();
                
                MenuRestoran menu;
                if ("MAKANAN".equals(kategori)) {
                    String pedas = view.getTxtTingkatPedas().getText();
                    menu = new Makanan(0, nama, harga, stok, pedas);
                } else {
                    boolean dingin = view.getChkDingin().isSelected();
                    menu = new Minuman(0, nama, harga, stok, dingin);
                }
                
                String idStr = view.getTxtId().getText();
                boolean success;
                
                if (idStr.isEmpty()) {
                    success = menuDAO.addMenu(menu);
                } else {
                    menu.setId(Integer.parseInt(idStr));
                    success = menuDAO.updateMenu(menu);
                }
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses!", "Data menu berhasil diamankan ke database.");
                    clearForm();
                    loadData(); // Tarik ulang biar tabelnya seger
                } else {
                    showAlert(Alert.AlertType.ERROR, "Waduh Gagal", "Ada masalah pas nyimpen ke database nih.");
                }
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Salah Format", "Pastikan isian Harga dan Stok hanya mengandung angka!");
            }
        });
        
        // Hapus Data
        view.getBtnDelete().setOnAction(e -> {
            MenuRestoran selected = view.getTableMenu().getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean success = menuDAO.deleteMenu(selected.getId());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Sayonara!", "Menu '" + selected.getNama() + "' resmi dihapus.");
                    clearForm();
                    loadData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal Hapus", "Sistem gagal menghapus data dari database.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Pilih Dulu Dong", "Klik salah satu menu di tabel sebelum menekan tombol hapus.");
            }
        });
        
        // Bersihkan Form
        view.getBtnClear().setOnAction(e -> clearForm());
    }

    private String[] parseCsvLine(String line) {
        java.util.List<String> list = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
