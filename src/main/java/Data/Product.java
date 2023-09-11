package Data;

public class Product {
    private long id;
    private String name;
    private String description;
    private String ean13;
    private double price;
    private double quantity;
    private double minStock;
    private String dtCreate;
    private String dtUpdate;
    private boolean l_ativo;

    // Getter para o campo 'id'
    public long getId() {
        return id;
    }

    // Setter para o campo 'id'
    public void setId(long id) {
        this.id = id;
    }

    // Getter para o campo 'name'
    public String getName() {
        return name;
    }

    // Setter para o campo 'name'
    public void setName(String name) {
        this.name = name;
    }

    // Getter para o campo 'description'
    public String getDescription() {
        return description;
    }

    // Setter para o campo 'description'
    public void setDescription(String description) {
        this.description = description;
    }

    // Getter para o campo 'ean13'
    public String getEan13() {
        return ean13;
    }

    // Setter para o campo 'ean13'
    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    // Getter para o campo 'price'
    public double getPrice() {
        return price;
    }

    // Setter para o campo 'price'
    public void setPrice(double price) {
        this.price = price;
    }

    // Getter para o campo 'quantity'
    public double getQuantity() {
        return quantity;
    }

    // Setter para o campo 'quantity'
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    // Getter para o campo 'minStock'
    public double getMinStock() {
        return minStock;
    }

    // Setter para o campo 'minStock'
    public void setMinStock(double minStock) {
        this.minStock = minStock;
    }

    // Getter para o campo 'dtCreate'
    public String getDtCreate() {
        return dtCreate;
    }

    // Setter para o campo 'dtCreate'
    public void setDtCreate(String dtCreate) {
        this.dtCreate = dtCreate;
    }

    // Getter para o campo 'dtUpdate'
    public String getDtUpdate() {
        return dtUpdate;
    }

    // Setter para o campo 'dtUpdate'
    public void setDtUpdate(String dtUpdate) {
        this.dtUpdate = dtUpdate;
    }

    // Getter para o campo 'active'
    public boolean isLativo() {
        return l_ativo;
    }

    // Setter para o campo 'active'
    public void setLativo(boolean l_ativo) {
        this.l_ativo = l_ativo;
    }

}
