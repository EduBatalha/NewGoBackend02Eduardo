package Infrastructure.dao;

import Infrastructure.PostgreSQLConnection;
import Infrastructure.Entity.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM produto");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getInt("id"));
                product.setHash(UUID.fromString(resultSet.getString("hash")));
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
            throw new RuntimeException(e);
        }
        return products;
    }

    // Consulta para retornar todos os produtos ativos
    public List<Product> getActiveProducts() {
        List<Product> activeProducts = new ArrayList<>();
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM produto WHERE lativo = true");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Product product = new Product();
                product.setId(resultSet.getInt("id"));
                product.setHash(UUID.fromString(resultSet.getString("hash")));
                product.setName(resultSet.getString("nome"));
                product.setDescription(resultSet.getString("descricao"));
                product.setEan13(resultSet.getString("ean13"));
                product.setPrice(resultSet.getDouble("preco"));
                product.setQuantity(resultSet.getDouble("quantidade"));
                product.setMinStock(resultSet.getDouble("estoque_min"));
                product.setLativo(resultSet.getBoolean("lativo"));
                activeProducts.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException(e);
        }
        return activeProducts;
    }

    // Consulta para retornar um produto pelo seu hash
    public Product getProductByHash(UUID productHash) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM produto WHERE hash = ?")) {
            statement.setObject(1, productHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getInt("id"));
                    product.setHash(UUID.fromString(resultSet.getString("hash")));
                    product.setName(resultSet.getString("nome"));
                    product.setDescription(resultSet.getString("descricao"));
                    product.setEan13(resultSet.getString("ean13"));
                    product.setPrice(resultSet.getDouble("preco"));
                    product.setQuantity(resultSet.getDouble("quantidade"));
                    product.setMinStock(resultSet.getDouble("estoque_min"));
                    product.setLativo(resultSet.getBoolean("lativo"));
                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException(e);
        }
        return null; // Retorna null se o produto não for encontrado ou não estiver ativo
    }

    // Consulta para retornar um produto ativo pelo seu hash
    public Product getActiveProductByHash(UUID productHash) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM produto WHERE hash = ? AND lativo = true")) {
            statement.setObject(1, productHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Product product = new Product();
                    product.setId(resultSet.getInt("id"));
                    product.setHash(UUID.fromString(resultSet.getString("hash")));
                    product.setName(resultSet.getString("nome"));
                    product.setDescription(resultSet.getString("descricao"));
                    product.setEan13(resultSet.getString("ean13"));
                    product.setPrice(resultSet.getDouble("preco"));
                    product.setQuantity(resultSet.getDouble("quantidade"));
                    product.setMinStock(resultSet.getDouble("estoque_min"));
                    product.setLativo(resultSet.getBoolean("lativo"));
                    return product;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException(e);
        }
        return null; // Retorna null se o produto não for encontrado ou não estiver ativo
    }


    public boolean doesProductExist(UUID productHash) {
        String query = "SELECT COUNT(*) FROM produto WHERE hash = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, productHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // Retorna true se houver algum registro com o hash
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Se ocorrer algum erro ou nenhum registro for encontrado
    }

    public boolean isProductActive(UUID productHash) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT lativo FROM produto WHERE hash = ?")) {
            statement.setObject(1, productHash);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    boolean lativo = resultSet.getBoolean("lativo");
                    return lativo;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Trate a exceção ou propague-a conforme necessário
            throw new RuntimeException(e);
        }
        return false; // Se ocorrer algum erro ou nenhum registro for encontrado
    }

    public boolean isProductNameDuplicate(String name) {
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

    public boolean isProductEan13Duplicate(String ean13) {
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


    public void createProduct(Product product) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO produto (nome, descricao, ean13, preco, quantidade, estoque_min, lativo, dtcreate) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Define a data de criação como a data e hora atual
            Date currentDate = new Date(System.currentTimeMillis());
            product.setDtCreate(currentDate);

            // Configure os valores dos parâmetros na consulta SQL
            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setString(3, product.getEan13());
            statement.setDouble(4, product.getPrice());
            statement.setDouble(5, product.getQuantity());
            statement.setDouble(6, product.getMinStock());
            statement.setBoolean(7, product.isLativo());
            statement.setTimestamp(8, new java.sql.Timestamp(product.getDtCreate().getTime())); // Converte Date para Timestamp

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean updateProduct(Product updatedProduct) {
        String sql = "UPDATE produto SET descricao = ?, preco = ?, quantidade = ?, estoque_min = ?, dtupdate = ? WHERE hash = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Define a data de atualização como a data e hora atual
            Date currentDate = new Date(System.currentTimeMillis());
            updatedProduct.setDtUpdate(currentDate);

            // Configure os valores dos parâmetros na consulta SQL
            preparedStatement.setString(1, updatedProduct.getDescription());
            preparedStatement.setDouble(2, updatedProduct.getPrice());
            preparedStatement.setDouble(3, updatedProduct.getQuantity());
            preparedStatement.setDouble(4, updatedProduct.getMinStock());
            preparedStatement.setTimestamp(5, new java.sql.Timestamp(updatedProduct.getDtUpdate().getTime())); // Converte Date para Timestamp
            preparedStatement.setObject(6, updatedProduct.getHash()); // Define o hash como parâmetro

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


    public void deleteProduct(UUID productHash) {
        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM produto WHERE hash = ?")) {

            statement.setObject(1, productHash);

            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void activateProduct(UUID productHash) {
        String sql = "UPDATE produto SET lativo = true WHERE hash = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, productHash); // Define o hash como parâmetro

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void deactivateProduct(UUID productHash) {
        String sql = "UPDATE produto SET lativo = false WHERE hash = ?";

        try (Connection connection = PostgreSQLConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setObject(1, productHash); // Define o hash como parâmetro

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}

