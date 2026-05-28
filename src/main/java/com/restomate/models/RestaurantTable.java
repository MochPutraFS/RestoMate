package com.restomate.models;

public class RestaurantTable {
    private int id;
    private int nomorMeja;
    private int kapasitas;

    public RestaurantTable(int id, int nomorMeja, int kapasitas) {
        this.id = id;
        this.nomorMeja = nomorMeja;
        this.kapasitas = kapasitas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNomorMeja() {
        return nomorMeja;
    }

    public void setNomorMeja(int nomorMeja) {
        this.nomorMeja = nomorMeja;
    }

    public int getKapasitas() {
        return kapasitas;
    }

    public void setKapasitas(int kapasitas) {
        this.kapasitas = kapasitas;
    }
}
