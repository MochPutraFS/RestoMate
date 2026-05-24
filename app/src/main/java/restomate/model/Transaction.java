package restomate.model;

public class Transaction {
    private int id;
    private double total;
    private String createdAt;

    public Transaction(int id, double total, String createdAt) {
        this.id = id;
        this.total = total;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}