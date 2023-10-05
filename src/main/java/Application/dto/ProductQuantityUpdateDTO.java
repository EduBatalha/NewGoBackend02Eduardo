package Application.dto;

import java.util.UUID;

public class ProductQuantityUpdateDTO {
    private UUID hash;
    private double quantidade;
    private String operacao; // Adicionando a operação

    public UUID getHash() {
        return hash;
    }

    public void setHash(UUID hash) {
        this.hash = hash;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }
}
