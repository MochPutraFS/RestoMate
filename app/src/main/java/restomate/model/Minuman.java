package restomate.model;

public class Minuman extends MenuRestoran {
    private boolean isDingin;
    private String kategori;

    public Minuman(int id, String nama, double harga, int stok, String deskripsi, String kategori, boolean isDingin) {
        super(id, nama, harga, stok, deskripsi);
        this.kategori = kategori;
        this.isDingin = isDingin;
    }

    public boolean isDingin() { return isDingin; }
    public void setDingin(boolean dingin) { isDingin = dingin; }

    @Override
    public void tampilkanDetail() {
        String suhu = isDingin ? "Dingin" : "Panas";
        System.out.println("Minuman: " + getNama() + " | Harga: " + getHarga() + " | Stok: " + getStok() + " | Kondisi: " + suhu);
    }

    @Override
    public String getKategori() {
        return kategori;
    }
}