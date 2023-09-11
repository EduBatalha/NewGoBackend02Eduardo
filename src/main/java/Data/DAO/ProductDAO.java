package Data.DAO;

import Data.PostgreSQLConnection;
import Data.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//TODO
public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM products");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getLong("id"));
                product.setName(resultSet.getString("name"));
                product.setDescription(resultSet.getString("description"));
                product.setEan13(resultSet.getString("ean13"));
                product.setPrice(resultSet.getDouble("price"));
                product.setQuantity(resultSet.getDouble("quantity"));
                product.setMinStock(resultSet.getDouble("min_stock"));
                product.setDtCreate(resultSet.getString("dtcreate"));
                product.setDtUpdate(resultSet.getString("dtupdate"));
                product.setLativo(resultSet.getBoolean("l_ativo"));
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
        String sql = "SELECT * FROM produtos WHERE id = ?";

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
        product.setId(resultSet.getLong("id"));
        product.setName(resultSet.getString("nome"));
        product.setDescription(resultSet.getString("descricao"));
        product.setEan13(resultSet.getString("ean13"));
        product.setPrice(resultSet.getDouble("preco"));
        product.setQuantity(resultSet.getDouble("quantidade"));
        product.setMinStock(resultSet.getDouble("estoque_min"));
        product.setDtCreate(resultSet.getString("dtcreate"));
        product.setDtUpdate(resultSet.getString("dtupdate"));
        product.setLativo(resultSet.getBoolean("lativo"));
        return product;
    }


    public void createProduct(Product product) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO products (name, description, ean13, price, quantity, min_stock, dtcreate, dtupdate, l_ativo) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getEan13());
            statement.setDouble(4, product.getPrice());
            statement.setDouble(5, product.getQuantity());
            statement.setDouble(6, product.getMinStock());
            statement.setString(7, product.getDtCreate());
            statement.setString(8, product.getDtUpdate());
            statement.setBoolean(9, product.isLativo());

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException("Erro ao criar um novo produto", e);
        }
    }

    public void updateProduct(Product product) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE products SET name = ?, description = ?, ean13 = ?, price = ?, " +
                             "quantity = ?, min_stock = ?, dtupdate = CURRENT_TIMESTAMP, l_ativo = ? WHERE id = ?")) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getEan13());
            statement.setDouble(4, product.getPrice());
            statement.setDouble(5, product.getQuantity());
            statement.setDouble(6, product.getMinStock());
            statement.setBoolean(7, product.isLativo());
            statement.setLong(8, product.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Falha ao atualizar o produto, ID não encontrado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException("Erro ao atualizar o produto", e);
        }
    }

    public void deleteProduct(long productId) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM products WHERE id = ?")) {

            statement.setLong(1, productId);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("Falha ao excluir o produto, ID não encontrado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException("Erro ao excluir o produto", e);
        }
    }
}

