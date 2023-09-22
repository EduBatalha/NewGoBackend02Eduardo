package Application.dto;

public class ProductDTO {
    private String nome;
    private String descricao;
    private String ean13;
    private double preco;
    private double quantidade;
    private double estoque_min;

    // Construtor vazio (necessário para Gson)
    public ProductDTO() {
    }

    public ProductDTO(String nome, String descricao, String ean13, double preco, double quantidade, double estoque_min) {
        this.nome = nome;
        this.descricao = descricao;
        this.ean13 = ean13;
        this.preco = preco;
        this.quantidade = quantidade;
        this.estoque_min = estoque_min;
    }

    // Getters e setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getEan13() {
        return ean13;
    }

    public void setEan13(String ean13) {
        this.ean13 = ean13;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public double getEstoqueMin() {
        return estoque_min;
    }

    public void setEstoqueMin(double estoque_min) {
        this.estoque_min = estoque_min;
    }
}
