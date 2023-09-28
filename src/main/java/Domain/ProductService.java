package Domain;

import Application.dto.ProductBatchDTO;
import Application.dto.ProductDTO;
import Infrastructure.dao.ProductDAO;
import Infrastructure.Entity.Product;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.*;


public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();

    private static final ResourceBundle messages = ResourceBundle.getBundle("messages");

    public List<Product> getAllProducts() {
        // Lógica para obter todos os produtos do banco de dados
        return productDAO.getAllProducts();
    }

    public List<Product> getActiveProducts() {
        // Lógica para obter todos os produtos ativos do banco de dados
        return productDAO.getActiveProducts();
    }

    public List<Product> getInactiveProducts() {
        return productDAO.getInactiveProducts();
    }

    public List<Product> getProductsBelowMinStock() {
        return productDAO.getProductsBelowMinStock();
    }


    public Product getActiveProductByHash(UUID productHash) {
        if (productDAO.doesProductExist(productHash)) {
            // Verifique se o produto está ativo
            if (productDAO.isProductActive(productHash)) {
                // Se estiver ativo, obtenha o produto
                return productDAO.getActiveProductByHash(productHash);
            } else {
                throw new IllegalArgumentException(messages.getString("error.productNotActive"));
            }
        } else {
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }
    }

    public Product getProductByHash(UUID productHash) {
        if (productDAO.doesProductExist(productHash)) {
            // Se o produto existir, obtenha o produto
            return productDAO.getProductByHash(productHash);
        } else {
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }
    }


    public void createProduct(Product product) {
        List<String> errors = new ArrayList<>();

        // Verificar duplicação de nome e EAN13 (RN002 e RN003)
        if (productDAO.isProductNameDuplicate(product.getName())) {
            errors.add(messages.getString("error.duplicateName"));
        }
        if (productDAO.isProductEan13Duplicate(product.getEan13())) {
            errors.add(messages.getString("error.duplicateEAN13"));
        }

        // Verificar se preço, quantidade ou estoque mínimo são negativos (RN004)
        if (product.getPrice() < 0) {
            errors.add(messages.getString("error.negativePrice"));
        }
        if (product.getQuantity() < 0) {
            errors.add(messages.getString("error.negativeQuantity"));
        }
        if (product.getMinStock() < 0) {
            errors.add(messages.getString("error.negativeMinStock"));
        }

        // Se houver erros, lance a exceção com a lista de erros
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errors));
        }

        // Se não houver erros, continue com a criação do produto
        product.setDtCreate(new Date());
        product.setLativo(false);
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

    public boolean activateOrDeactivateProduct(UUID productHash, boolean isActive) {
        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(productHash);

        if (!productExists) {
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }

        if (isActive) {
            // Ativar o produto
            productDAO.activateProduct(productHash);
        } else {
            // Desativar o produto
            productDAO.deactivateProduct(productHash);
        }

        return true;
    }

    public JsonObject createProductsInBatch(ProductBatchDTO batchDTO) {
        // Inicialize listas para rastrear produtos com sucesso e erros
        List<JsonObject> errorProducts = new ArrayList<>();

        // Acesse a lista de produtos do DTO
        List<ProductDTO> products = batchDTO.getProductDTOs();
        boolean success = false; // Variável para rastrear o sucesso

        // Itere sobre cada produto no lote
        for (ProductDTO productDTO : products) {
            try {
                // Converte o DTO em um objeto Product
                Product product = convertProductDTO(productDTO);

                // Tente cadastrar o produto
                createProduct(product);

                // Se o produto for cadastrado com sucesso, defina a variável de sucesso como verdadeira
                success = true;
            } catch (IllegalArgumentException e) {
                // Se ocorrer uma exceção, capture o erro e crie um objeto JSON para representar o erro
                JsonObject error = new JsonObject();
                error.addProperty("nome", productDTO.getNome());
                error.addProperty("ean13", productDTO.getEan13());
                error.addProperty("error", e.getMessage());
                errorProducts.add(error);
            }
        }

        // Crie um objeto JSON para representar o resultado
        JsonObject result = new JsonObject();
        result.add("errors", gson.toJsonTree(errorProducts));

        // Se algum produto foi cadastrado com sucesso, adicione a parte de sucesso
        if (success) {
            result.addProperty("sucesso", "Produto(s) cadastrado(s) com sucesso");
        }

        return result;
    }




    // Método auxiliar para converter ProductDTO em Product
    private Product convertProductDTO(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getNome());
        product.setDescription(productDTO.getDescricao());
        product.setEan13(productDTO.getEan13());
        product.setPrice(productDTO.getPreco());
        product.setQuantity(productDTO.getQuantidade());
        product.setMinStock(productDTO.getEstoqueMin());
        return product;
    }

}
