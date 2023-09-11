package Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostgreSQLConnection {
    private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/estoque";
    private static final String USER = "postgres";
    private static final String PASSWORD = "0000";

    public static Connection getConnection() {
        try {
            // Carregando o driver JDBC do PostgreSQL
            Class.forName("org.postgresql.Driver");

            // Estabelecendo a conexão com o banco de dados
            Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            // Trate as exceções aqui ou simplesmente propague-as
            throw new RuntimeException("Erro ao obter a conexão com o banco de dados", e);
        }
    };

    public static void main(String[] args) {
        System.out.println("Testando a conexão com o banco de dados...");

        // Obtendo uma conexão com o PostgreSQL
        Connection connection = getConnection();

        if (connection != null) {
            System.out.println("Conexão bem-sucedida ao banco de dados PostgreSQL.");

            // Feche a conexão quando terminar
            try {
                connection.close();
                System.out.println("Conexão com o banco de dados fechada com sucesso.");
            } catch (SQLException e) {
                System.out.println("Erro ao fechar a conexão com o banco de dados.");
                e.printStackTrace();
            }
        } else {
            System.out.println("Falha na conexão ao banco de dados PostgreSQL.");
        }
    }
}