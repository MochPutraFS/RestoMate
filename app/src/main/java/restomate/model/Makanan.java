package restomate.model;

public class Makanan extends MenuRestoran {
    private int tingkatPedas;
    private String kategori;

    public Makanan(int id, String nama, double harga, int stok, String deskripsi, String kategori, int tingkatPedas) {
        super(id, nama, harga, stok, deskripsi);
        this.kategori = kategori;
        this.tingkatPedas = tingkatPedas;
    }

    public int getTingkatPedas() { return tingkatPedas; }
    public void setTingkatPedas(int tingkatPedas) { this.tingkatPedas = tingkatPedas; }

    @Override
    public void tampilkanDetail() {
        System.out.println("Makanan: " + getNama() + " | Harga: " + getHarga() + " | Stok: " + getStok() + " | Tingkat Pedas: " + tingkatPedas);
    }

    @Override
    public String getKategori() {
        return kategori;
    }
}