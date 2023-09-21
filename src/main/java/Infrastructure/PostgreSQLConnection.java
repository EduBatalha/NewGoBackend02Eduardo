package Infrastructure;
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

            // Retorna a conexão dentro do bloco try-with-resources
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}