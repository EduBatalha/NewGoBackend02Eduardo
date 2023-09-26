package Application.dto;

public class ProductUpdateDTO {
    private String hash;
    private String descricao;
    private double preco;
    private double quantidade;
    private double estoque_min;

    // Getters e setters para os campos desejados
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
