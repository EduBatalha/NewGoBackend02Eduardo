package Service;

import Data.DAO.ProductDAO;
import Data.PostgreSQLConnection;
import Data.Product;

import java.sql.*;
import java.util.List;
import java.util.UUID;


public class ProductService {
    private ProductDAO productDAO = new ProductDAO();

    public List<Product> getAllProducts() {
        // Lógica para obter todos os produtos do banco de dados
        return productDAO.getAllProducts();
    }

    public void createProduct(Product product) {
        // Verificar duplicação de nome e EAN13 (RN002 e RN003)
        if (isProductNameDuplicate(product.getName()) || isProductEan13Duplicate(product.getEan13())) {
            throw new IllegalArgumentException("Nome ou EAN13 já existem no banco de dados");
        }

        // Verificar se preço, quantidade ou estoque mínimo são negativos (RN004)
        if (product.getPrice() < 0 || product.getQuantity() < 0 || product.getMinStock() < 0) {
            throw new IllegalArgumentException("Preço, quantidade ou estoque mínimo não podem ser negativos");
        }

        // Preencher lativo com falso (RN009)
        product.setLativo(false);

        // Chamar o método do DAO para criar o produto
        productDAO.createProduct(product);
    }


    private boolean isProductNameDuplicate(String name) {
        // Implemente a lógica de verificação de duplicação usando a conexão do PostgreSQLConnection
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM produto WHERE nome = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isProductEan13Duplicate(String ean13) {
        // Implemente a lógica de verificação de duplicação usando a conexão do PostgreSQLConnection
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM produto WHERE ean13 = ?")) {
            statement.setString(1, ean13);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateProduct(UUID productHash, Product updatedProduct) {
        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(productHash);

        if (!productExists) {
            throw new IllegalArgumentException("Produto não encontrado");
        }

        // Verificar se o campo 'lativo' é verdadeiro antes de permitir a atualização
        if (!productDAO.isProductActive(productHash)) {
            throw new IllegalArgumentException("Não é possível atualizar um produto inativo");
        }

        // Atualize o produto no banco de dados usando o ProductDAO
        return productDAO.updateProduct(updatedProduct);
    }




    public void deleteProduct(UUID productHash) {
        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(productHash);

        if (!productExists) {
            // Produto não encontrado, tratar a situação de acordo, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Produto não encontrado");
        }

        // Chame o método do DAO para excluir o produto usando o hash
        productDAO.deleteProduct(productHash);
    }


}
