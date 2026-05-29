package com.restomate.models;

import java.time.LocalDateTime;

public class Reservation {
    private int id;
    private String namaPelanggan;
    private int nomorMeja;
    private LocalDateTime waktuReservasi;
    private String status;
    private int jumlahOrang;
    private String menuDipesan;
    private String catatan;
    private String waktuSiap;
    private double biayaTotal;
    private double dpDibayar;

    public Reservation(int id, String namaPelanggan, int nomorMeja, LocalDateTime waktuReservasi, String status) {
        this(id, namaPelanggan, nomorMeja, waktuReservasi, status, 1, "", "", "", 0.0, 0.0);
    }

    public Reservation(int id, String namaPelanggan, int nomorMeja, LocalDateTime waktuReservasi, String status, int jumlahOrang, String menuDipesan, String catatan, String waktuSiap) {
        this(id, namaPelanggan, nomorMeja, waktuReservasi, status, jumlahOrang, menuDipesan, catatan, waktuSiap, 0.0, 0.0);
    }

    public Reservation(int id, String namaPelanggan, int nomorMeja, LocalDateTime waktuReservasi, String status, int jumlahOrang, String menuDipesan, String catatan, String waktuSiap, double biayaTotal, double dpDibayar) {
        this.id = id;
        this.namaPelanggan = namaPelanggan;
        this.nomorMeja = nomorMeja;
        this.waktuReservasi = waktuReservasi;
        this.status = status;
        this.jumlahOrang = jumlahOrang;
        this.menuDipesan = menuDipesan;
        this.catatan = catatan;
        this.waktuSiap = waktuSiap;
        this.biayaTotal = biayaTotal;
        this.dpDibayar = dpDibayar;
    }
    
    public Reservation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public int getNomorMeja() { return nomorMeja; }
    public void setNomorMeja(int nomorMeja) { this.nomorMeja = nomorMeja; }

    public LocalDateTime getWaktuReservasi() { return waktuReservasi; }
    public void setWaktuReservasi(LocalDateTime waktuReservasi) { this.waktuReservasi = waktuReservasi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getJumlahOrang() { return jumlahOrang; }
    public void setJumlahOrang(int jumlahOrang) { this.jumlahOrang = jumlahOrang; }

    public String getMenuDipesan() { return menuDipesan; }
    public void setMenuDipesan(String menuDipesan) { this.menuDipesan = menuDipesan; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public String getWaktuSiap() { return waktuSiap; }
    public void setWaktuSiap(String waktuSiap) { this.waktuSiap = waktuSiap; }

    public double getBiayaTotal() { return biayaTotal; }
    public void setBiayaTotal(double biayaTotal) { this.biayaTotal = biayaTotal; }

    public double getDpDibayar() { return dpDibayar; }
    public void setDpDibayar(double dpDibayar) { this.dpDibayar = dpDibayar; }
}
