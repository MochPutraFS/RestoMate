package restomate.model;

public class Reservation {
    private int id;
    private String namaPelanggan;
    private int nomorMeja;
    private String tanggalReservasi;
    private String jamReservasi;
    private String status;

    public Reservation(int id, String namaPelanggan, int nomorMeja, String tanggalReservasi, String jamReservasi, String status) {
        this.id = id;
        this.namaPelanggan = namaPelanggan;
        this.nomorMeja = nomorMeja;
        this.tanggalReservasi = tanggalReservasi;
        this.jamReservasi = jamReservasi;
        this.status = status;
    }

    public int getId() { return id; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public int getNomorMeja() { return nomorMeja; }
    public String getTanggalReservasi() { return tanggalReservasi; }
    public String getJamReservasi() { return jamReservasi; }
    public String getStatus() { return status; }
}