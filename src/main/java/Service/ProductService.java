package Service;

import Data.DAO.ProductDAO;
import Data.PostgreSQLConnection;
import Data.Product;

import java.sql.*;
import java.util.List;


//TODO
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

        // Preencher dtcreate com a timestamp atual, dtupdate com nulo e lativo com falso (RN009)
        product.setDtCreate(getCurrentTimestamp());
        product.setDtUpdate(null);
        product.setLativo(false);

        // Chamar o método do DAO para criar o produto
        productDAO.createProduct(product);
    }

    private boolean isProductNameDuplicate(String name) {
        // Implemente a lógica de verificação de duplicação usando a conexão do PostgreSQLConnection
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM products WHERE name = ?")) {
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
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM products WHERE ean13 = ?")) {
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

    private String getCurrentTimestamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return timestamp.toString();
    }



    public void updateProduct(Product product) {
        // Adicione lógica para verificar se o produto existe no banco de dados, por exemplo, pelo ID
        Product existingProduct = productDAO.getProductById(product.getId());
        if (existingProduct == null) {
            // Produto não encontrado, tratar a situação de acordo, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Produto não encontrado");
        }

        // Adicione lógica para verificar se o produto está inativo (RN012)
        if (!existingProduct.isLativo() && !product.isLativo()) {
            // Produto está inativo e não pode ser atualizado, tratar a situação de acordo, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Produto inativo não pode ser atualizado");
        }

        // Adicione lógica para verificar regras de negócios, por exemplo, RN004
        if (product.getPrice() < 0 || product.getQuantity() < 0 || product.getMinStock() < 0) {
            // Lidar com valores negativos, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Preço, quantidade ou estoque mínimo não podem ser negativos");
        }

        // Atualize dtupdate com a timestamp atual (RN011)
        product.setDtUpdate(getCurrentTimestamp());

        // Chame o método do DAO para atualizar o produto
        productDAO.updateProduct(product);
    }


    public void deleteProduct(long productId) {
        // Adicione lógica para verificar se o produto existe no banco de dados, por exemplo, pelo ID
        Product existingProduct = productDAO.getProductById(productId);
        if (existingProduct == null) {
            // Produto não encontrado, tratar a situação de acordo, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Produto não encontrado");
        }

        // Adicione lógica para verificar se o produto está inativo (RN012)
        if (!existingProduct.isLativo()) {
            // Produto está inativo e não pode ser excluído, tratar a situação de acordo, por exemplo, lançar uma exceção
            throw new IllegalArgumentException("Produto inativo não pode ser excluído");
        }

        // Chame o método do DAO para excluir o produto
        productDAO.deleteProduct(productId);
    }

}
