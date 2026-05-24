package restomate.model;

public class CartItem {
    private MenuRestoran menu;
    private int jumlah;
    private String catatan;

    public CartItem(MenuRestoran menu, int jumlah) {
        this.menu = menu;
        this.jumlah = jumlah;
        this.catatan = "";
    }

    public MenuRestoran getMenu() { return menu; }

    public String getNamaMenu() { return menu.getNama(); }
    
    public double getHargaSatuan() { return menu.getHarga(); }
    
    public int getJumlah() { return jumlah; }
    
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }

    public double getSubtotal() { return menu.getHarga() * jumlah; }

    public String getCatatan() { return catatan; }

    public void setCatatan(String catatan) { this.catatan = catatan; }
}