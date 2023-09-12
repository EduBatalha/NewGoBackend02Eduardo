package Data.DAO;

import Data.PostgreSQLConnection;
import Data.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM produto");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getInt("id"));
                product.setName(resultSet.getString("nome"));
                product.setDescription(resultSet.getString("descricao"));
                product.setEan13(resultSet.getString("ean13"));
                product.setPrice(resultSet.getDouble("preco"));
                product.setQuantity(resultSet.getDouble("quantidade"));
                product.setMinStock(resultSet.getDouble("estoque_min"));
                product.setLativo(resultSet.getBoolean("lativo"));
                products.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException("Erro ao obter a lista de produtos", e);
        }
        return products;
    }


    public Product getProductById(long productId) {
        String sql = "SELECT * FROM produto WHERE id = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSetToProduct(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Lidar com exceções aqui
        }

        return null; // Retorne null se o produto não for encontrado
    }


    // Adicione um método para mapear o resultado da consulta para um objeto Product
    private Product mapResultSetToProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getInt("id"));
        product.setName(resultSet.getString("nome"));
        product.setDescription(resultSet.getString("descricao"));
        product.setEan13(resultSet.getString("ean13"));
        product.setPrice(resultSet.getDouble("preco"));
        product.setQuantity(resultSet.getDouble("quantidade"));
        product.setMinStock(resultSet.getDouble("estoque_min"));
        product.setLativo(resultSet.getBoolean("lativo"));
        return product;
    }


    public void createProduct(Product product) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO produto (nome, descricao, ean13, preco, quantidade, estoque_min, lativo) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getEan13());
            statement.setDouble(4, product.getPrice());
            statement.setDouble(5, product.getQuantity());
            statement.setDouble(6, product.getMinStock());
            statement.setBoolean(7, product.isLativo());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar um novo produto", e);
        }
    }

    public boolean updateProduct(Product updatedProduct) {
        String sql = "UPDATE produtos SET nome = ?, descricao = ?, ean13 = ?, preco = ?, quantidade = ?, estoque_min = ?, lativo = ? WHERE id = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Define os valores dos parâmetros na consulta SQL
            preparedStatement.setString(1, updatedProduct.getName());
            preparedStatement.setString(2, updatedProduct.getDescription());
            preparedStatement.setString(3, updatedProduct.getEan13());
            preparedStatement.setDouble(4, updatedProduct.getPrice());
            preparedStatement.setDouble(5, updatedProduct.getQuantity());
            preparedStatement.setDouble(6, updatedProduct.getMinStock());
            preparedStatement.setBoolean(7, updatedProduct.isLativo());
            preparedStatement.setLong(8, updatedProduct.getId());

            // Executa a consulta de atualização
            int rowsUpdated = preparedStatement.executeUpdate();

            // Verifica se a atualização foi bem-sucedida
            return rowsUpdated > 0;
        } catch (SQLException e) {
            // Lida com exceções SQL, como conexão perdida, consulta inválida, etc.
            e.printStackTrace();
            return false;
        }
    }



    public void deleteProduct(int productId) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM produto WHERE id = ?")) {

            statement.setInt(1, productId);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("Falha ao excluir o produto, ID não encontrado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao excluir o produto", e);
        }
    }
}

