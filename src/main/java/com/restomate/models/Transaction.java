package com.restomate.models;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private double total;
    private String metodePembayaran;
    private String catatan;
    private LocalDateTime createdAt;

    public Transaction(int id, double total, String metodePembayaran, String catatan, LocalDateTime createdAt) {
        this.id = id;
        this.total = total;
        this.metodePembayaran = metodePembayaran;
        this.catatan = catatan;
        this.createdAt = createdAt;
    }
    
    public Transaction() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    private String namaPelanggan;
    private String nomorAntrian;

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getNomorAntrian() { return nomorAntrian; }
    public void setNomorAntrian(String nomorAntrian) { this.nomorAntrian = nomorAntrian; }

    private String tipePesanan;

    public String getTipePesanan() { return tipePesanan; }
    public void setTipePesanan(String tipePesanan) { this.tipePesanan = tipePesanan; }
}
