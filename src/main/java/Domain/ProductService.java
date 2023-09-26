package Domain;

import Infrastructure.dao.ProductDAO;
import Infrastructure.Entity.Product;


import java.util.List;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.UUID;


public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    private static final ResourceBundle messages = ResourceBundle.getBundle("messages");

    public List<Product> getAllProducts() {
        // Lógica para obter todos os produtos do banco de dados
        return productDAO.getAllProducts();
    }

    public void createProduct(Product product) {
        // Verificar duplicação de nome e EAN13 (RN002 e RN003)
        if (productDAO.isProductNameDuplicate(product.getName())) {
            throw new IllegalArgumentException(messages.getString("error.duplicateName"));
        }

        if (productDAO.isProductEan13Duplicate(product.getEan13())) {
            throw new IllegalArgumentException(messages.getString("error.duplicateEAN13"));
        }

        // Verificar se preço, quantidade ou estoque mínimo são negativos (RN004)
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException(messages.getString("error.negativePrice"));
        }
        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException(messages.getString("error.negativeQuantity"));
        }

        if (product.getMinStock() < 0) {
            throw new IllegalArgumentException(messages.getString("error.negativeMinStock"));
        }

        // Define a data de criação como a data e hora atual
        product.setDtCreate(new Date());

        // Preencher lativo com falso (RN009)
        product.setLativo(false);

        // Chamar o método do DAO para criar o produto
        productDAO.createProduct(product);
    }


    public boolean updateProduct(UUID productHash, Product updatedProduct) {
        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(productHash);

        if (!productExists) {
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }

        // Verificar se o campo 'lativo' é verdadeiro antes de permitir a atualização
        if (!productDAO.isProductActive(productHash)) {
            throw new IllegalArgumentException(messages.getString("error.cannotUpdateInactiveProduct"));
        }

        // Define a data de atualização como a data e hora atual
        updatedProduct.setDtUpdate(new Date());

        // Atualize o produto no banco de dados usando o ProductDAO
        return productDAO.updateProduct(updatedProduct);
    }


    public void deleteProduct(UUID productHash) {
        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(productHash);

        if (!productExists) {
            // Produto não encontrado
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }

        // Chame o método do DAO para excluir o produto usando o hash
        productDAO.deleteProduct(productHash);
    }

}
