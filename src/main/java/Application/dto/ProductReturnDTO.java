package Application.dto;

import java.util.Date;
import java.util.UUID;

public class ProductReturnDTO {
    private UUID hash;
    private String nome;
    private String descricao;
    private String ean13;
    private double preco;
    private double quantidade;
    private double estoque_min;
    private boolean lativo;
    private Date dtCreate;
    private Date dtUpdate;

    public ProductReturnDTO() {
        // Construtor vazio
    }

    public UUID getHash() {
        return hash;
    }

    public void setHash(UUID hash) {
        this.hash = hash;
    }

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

    public double getEstoque_min() {
        return estoque_min;
    }

    public void setEstoque_min(double estoque_min) {
        this.estoque_min = estoque_min;
    }

    public boolean isLativo() {
        return lativo;
    }

    public void setLativo(boolean lativo) {
        this.lativo = lativo;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public void setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public void setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
    }
}
