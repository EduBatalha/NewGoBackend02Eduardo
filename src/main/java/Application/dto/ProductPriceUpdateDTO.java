package Application.dto;

import java.util.UUID;

public class ProductPriceUpdateDTO {
    private UUID hash;
    private String operacao; // Pode ser "somar", "subtrair" ou "definir"
    private String valor; // Pode ser um número ou uma porcentagem representada como "20%"

    public UUID getHash() {
        return hash;
    }

    public void setHash(UUID hash) {
        this.hash = hash;
    }

    public String getOperacao() {
        return operacao;
    }

    public void setOperacao(String operacao) {
        this.operacao = operacao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}
