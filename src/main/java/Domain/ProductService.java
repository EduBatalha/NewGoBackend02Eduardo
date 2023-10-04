package Domain;

import Application.dto.*;
import Infrastructure.dao.ProductDAO;
import Infrastructure.Entity.Product;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.*;


public class ProductService {
    private ProductDAO productDAO = new ProductDAO();
    private Gson gson = new Gson();
    private static final ResourceBundle messages = ResourceBundle.getBundle("messages");


    public List<ProductReturnDTO> getAllProducts() {
        // Lógica para obter todos os produtos do banco de dados
        return productDAO.getAllProducts();
    }


    public List<ProductReturnDTO> getActiveProducts() {
        // Lógica para obter todos os produtos ativos do banco de dados
        return productDAO.getActiveProducts();
    }


    public List<ProductReturnDTO> getInactiveProducts() {
        return productDAO.getInactiveProducts();
    }


    public List<ProductReturnDTO> getProductsBelowMinStock() {
        return productDAO.getProductsBelowMinStock();
    }


    public ProductReturnDTO getActiveProductByHash(UUID productHash) {
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


    public Product createProduct(Product product) {
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
        product.setHash(UUID.randomUUID());
        product.setDtCreate(new Date());
        product.setLativo(false);
        productDAO.createProduct(product);
        return product;
    }


    public JsonObject createProductsInBatch(ProductBatchDTO batchDTO) {
        // Inicialize listas para rastrear produtos com sucesso e erros
        List<JsonObject> errorProducts = new ArrayList<>();
        List<Product> successProducts = new ArrayList<>(); // Alteração aqui

        // Acesse a lista de produtos do DTO
        List<ProductDTO> products = batchDTO.getProductDTOs();

        // Itere sobre cada produto no lote
        for (ProductDTO productDTO : products) {
            try {
                // Converte o DTO em um objeto Product
                Product product = convertProductDTO(productDTO);

                // Tente cadastrar o produto
                Product createdProduct = createProduct(product); // Retorna o produto criado

                // Adicione o produto criado à lista de sucesso
                successProducts.add(createdProduct);
            } catch (IllegalArgumentException e) {
                // Se ocorrer uma exceção, capture o erro e crie um objeto JSON para representar o erro
                JsonObject errorProduct = new JsonObject();
                errorProduct.addProperty("nome", productDTO.getNome());
                errorProduct.addProperty("ean13", productDTO.getEan13());
                errorProduct.addProperty("error", e.getMessage());
                errorProducts.add(errorProduct);
            }
        }

        // Crie um objeto JSON para representar o resultado
        JsonObject result = new JsonObject();

        // Adicione a lista de produtos com sucesso ao resultado
        if (!successProducts.isEmpty()) {
            result.add("success", gson.toJsonTree(successProducts));
        }

        // Adicione a lista de produtos com erro ao resultado
        if (!errorProducts.isEmpty()) {
            result.add("error", gson.toJsonTree(errorProducts));
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


    public Product updateProduct(ProductUpdateDTO updateDTO) {
        // Verifique se os campos obrigatórios estão presentes
        List<String> missingFields = new ArrayList<>();
        if (updateDTO.getHash() == null) {
            missingFields.add("hash");
        }
        if (updateDTO.getDescricao() == null) {
            missingFields.add("descricao");
        }
        if (updateDTO.getPreco() <= 0) {
            missingFields.add("preco");
        }
        if (updateDTO.getQuantidade() <= 0) {
            missingFields.add("quantidade");
        }
        if (updateDTO.getEstoqueMin() <= 0) {
            missingFields.add("estoque_min");
        }

        if (!missingFields.isEmpty()) {
            // Campos obrigatórios ausentes, lançar uma exceção com a lista de campos ausentes
            throw new IllegalArgumentException("Campos obrigatórios ausentes: " + String.join(", ", missingFields));
        }

        // Obtenha o hash do DTO
        String productHash = updateDTO.getHash();

        // Verificar se o produto com o hash especificado existe no banco de dados
        boolean productExists = productDAO.doesProductExist(UUID.fromString(productHash));

        if (!productExists) {
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }

        // Verificar se o campo 'lativo' é verdadeiro antes de permitir a atualização
        if (!productDAO.isProductActive(UUID.fromString(productHash))) {
            throw new IllegalArgumentException(messages.getString("error.cannotUpdateInactiveProduct"));
        }

        // Crie um objeto Product e configure-o com base no DTO
        Product updatedProduct = new Product();
        updatedProduct.setHash(UUID.fromString(productHash));
        updatedProduct.setDescription(updateDTO.getDescricao());
        updatedProduct.setPrice(updateDTO.getPreco());
        updatedProduct.setQuantity(updateDTO.getQuantidade());
        updatedProduct.setMinStock(updateDTO.getEstoqueMin());

        // Define a data de atualização como a data e hora atual
        updatedProduct.setDtUpdate(new Date());

        // Restante do código para atualizar o produto no banco de dados usando o ProductDAO
        boolean updateSuccess = productDAO.updateProduct(updatedProduct);

        if (updateSuccess) {
            // Após a atualização bem-sucedida, obtenha o produto atualizado da classe DAO
            return productDAO.getProductByHash(UUID.fromString(productHash));
        } else {
            throw new RuntimeException(messages.getString("error.productUpdateFailed"));
        }
    }


    public List<JsonObject> updateProductPricesInBatch(List<ProductPriceUpdateDTO> updates) {
        List<JsonObject> produtosAtualizados = new ArrayList<>();

        for (ProductPriceUpdateDTO update : updates) {
            Product product = productDAO.getProductByHash(update.getHash());

            // Verifique se o produto está ativo antes de atualizar
            if (!product.isLativo()) {
                JsonObject erroProduto = new JsonObject();
                erroProduto.addProperty("hash", update.getHash().toString());
                erroProduto.addProperty("error", messages.getString("error.cannotUpdateInactiveProduct") + ": " + update.getHash());
                produtosAtualizados.add(erroProduto);
            } else {
                // Obtenha o preço atual do produto
                double precoAtual = product.getPrice();

                // Calcule o novo preço com base no preço atual e na operação
                double novoPreco = calcularNovoPreco(precoAtual, update);

                // Atualize o campo "preco" do produto diretamente
                product.setPrice(novoPreco);

                // Tente atualizar o produto no banco de dados
                boolean atualizacaoBemSucedida = productDAO.updateProduct(product);

                if (atualizacaoBemSucedida) {
                    JsonObject produtoAtualizado = new JsonObject();
                    produtoAtualizado.addProperty("hash", product.getHash().toString());
                    produtoAtualizado.addProperty("nome", product.getName());
                    produtoAtualizado.addProperty("descricao", product.getDescription());
                    produtoAtualizado.addProperty("ean13", product.getEan13());
                    produtoAtualizado.addProperty("preco", precoAtual + " -> " + novoPreco);
                    produtoAtualizado.addProperty("quantidade", product.getQuantity());
                    produtoAtualizado.addProperty("estoque_min", product.getMinStock());
                    produtoAtualizado.addProperty("lativo", product.isLativo());
                    produtoAtualizado.addProperty("dtCreate", product.getDtCreate().toString());
                    produtoAtualizado.addProperty("dtUpdate", product.getDtUpdate().toString());

                    produtosAtualizados.add(produtoAtualizado);
                } else {
                    JsonObject erroProduto = new JsonObject();
                    erroProduto.addProperty("hash", update.getHash().toString());
                    erroProduto.addProperty("error", messages.getString("product.update.error") + update.getHash());
                    produtosAtualizados.add(erroProduto);
                }
            }
        }

        return produtosAtualizados;
    }





    private double calcularNovoPreco(double precoAtual, ProductPriceUpdateDTO update) {
        double valorParaAdicionar = parseValor(update.getValor());

        if (update.getOperacao().equalsIgnoreCase("subtrair")) {
            // Desconto percentual (ex: -20%)
            return precoAtual * (1.0 - valorParaAdicionar);
        } else if (update.getOperacao().equalsIgnoreCase("somar")) {
            // Aumento percentual (ex: +20%)
            return precoAtual * (1.0 + valorParaAdicionar);
        } else if (update.getOperacao().equalsIgnoreCase("definir")) {
            // Definir para um valor específico
            return valorParaAdicionar;
        } else {
            throw new IllegalArgumentException("Operação inválida: " + update.getOperacao());
        }
    }






    // Método para analisar e converter o valor (porcentagem ou valor fixo)
    private double parseValor(String valor) {
        if (valor.endsWith("%")) {
            // Valor é uma porcentagem
            double percent = Double.parseDouble(valor.replace("%", ""));
            return percent / 100.0; // Converter para a fração correspondente
        } else {
            // Valor é um número
            return Double.parseDouble(valor);
        }
    }


    // Método para atualizar o campo "preco" de um produto
    private void updateProductPrice(Product product, double novoPreco) {
        product.setPrice(novoPreco);
    }


    public JsonObject updateProductQuantitiesInBatch(ProductQuantityUpdateDTO[] updates) {
        List<JsonObject> errorProducts = new ArrayList<>();
        List<JsonObject> successProducts = new ArrayList<>();

        for (ProductQuantityUpdateDTO update : updates) {
            Product product = productDAO.getProductByHash(update.getHash());

            if (product != null) {
                // Verificar se a quantidade é não negativa
                if (update.getQuantidade() >= 0) {
                    // Verificar se o produto está ativo antes de atualizar
                    if (product.isLativo()) {
                        // Atualize a quantidade do produto
                        product.setQuantity(update.getQuantidade());
                        boolean atualizacaoBemSucedida = productDAO.updateProduct(product);

                        if (atualizacaoBemSucedida) {
                            JsonObject successProduct = new JsonObject();
                            successProduct.addProperty("hash", product.getHash().toString());
                            successProduct.addProperty("quantidade", product.getQuantity());
                            successProducts.add(successProduct);
                        } else {
                            JsonObject errorProduct = new JsonObject();
                            errorProduct.addProperty("hash", product.getHash().toString());
                            errorProduct.addProperty("error", messages.getString("product.update.error"));
                            errorProducts.add(errorProduct);
                        }
                    } else {
                        // Produto inativo, lançar exceção
                        JsonObject errorProduct = new JsonObject();
                        errorProduct.addProperty("hash", product.getHash().toString());
                        errorProduct.addProperty("error", messages.getString("error.cannotUpdateInactiveProduct"));
                        errorProducts.add(errorProduct);
                    }
                } else {
                    JsonObject errorProduct = new JsonObject();
                    errorProduct.addProperty("hash", product.getHash().toString());
                    errorProduct.addProperty("error", messages.getString("product.negativeQuantity"));
                    errorProducts.add(errorProduct);
                }
            } else {
                JsonObject errorProduct = new JsonObject();
                errorProduct.addProperty("hash", update.getHash().toString());
                errorProduct.addProperty("error", messages.getString("product.productNotFound"));
                errorProducts.add(errorProduct);
            }
        }

        JsonObject result = new JsonObject();

        if (!successProducts.isEmpty()) {
            result.add("success", gson.toJsonTree(successProducts));
        }

        if (!errorProducts.isEmpty()) {
            result.add("error", gson.toJsonTree(errorProducts));
        }

        return result;
    }


    public ProductReturnDTO deleteProduct(UUID productHash) {
        // Buscar o produto pelo hash
        Product productToDelete = productDAO.getProductByHash(productHash);

        if (productToDelete == null) {
            // Produto não encontrado
            throw new IllegalArgumentException(messages.getString("error.productNotFound"));
        }

        // Chame o método do DAO para excluir o produto usando o hash
        productDAO.deleteProduct(productHash);

        // Crie um novo objeto ProductReturnDTO manualmente a partir do objeto Product
        ProductReturnDTO productReturnDTO = new ProductReturnDTO();
        productReturnDTO.setHash(productToDelete.getHash());
        productReturnDTO.setNome(productToDelete.getName());
        productReturnDTO.setDescricao(productToDelete.getDescription());
        productReturnDTO.setEan13(productToDelete.getEan13());
        productReturnDTO.setPreco(productToDelete.getPrice());
        productReturnDTO.setQuantidade(productToDelete.getQuantity());
        productReturnDTO.setEstoque_min(productToDelete.getMinStock());
        productReturnDTO.setLativo(productToDelete.isLativo());
        productReturnDTO.setDtCreate(productToDelete.getDtCreate());
        productReturnDTO.setDtUpdate(productToDelete.getDtUpdate());

        return productReturnDTO; // Retorna o produto que foi excluído como ProductReturnDTO
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
}
