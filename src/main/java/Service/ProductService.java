package Service;

import Data.DAO.ProductDAO;
import Data.Product;

import java.util.List;


//TODO
public class ProductService {
    private ProductDAO productDAO = new ProductDAO();

    public List<Product> getAllProducts() {
        // Lógica para obter todos os produtos do banco de dados
        return productDAO.getAllProducts();
    }

    public void createProduct(Product product) {
        // Lógica para criar um novo produto no banco de dados
    }

    public void updateProduct(Product product) {
        // Lógica para atualizar um produto existente no banco de dados
    }

    public void deleteProduct(long productId) {
        // Lógica para excluir um produto com base no ID
    }
}
