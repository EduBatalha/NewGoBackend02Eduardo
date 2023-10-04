package Application.Servlet;

import Application.dto.*;
import Infrastructure.Entity.Product;
import Infrastructure.dao.ProductDAO;
import Domain.ProductService;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.*;

@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {
    private ProductService productService = new ProductService();
    private ProductDAO productDAO = new ProductDAO();
    ResourceBundle messages = ResourceBundle.getBundle("messages");
    private Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(request.getMethod())) {
            doPatch(request, response);
        } else {
            super.service(request, response);
        }
    }



    //Método GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestURI = request.getRequestURI();
            String[] parts = requestURI.split("/");

            if (parts.length >= 3 && "products".equals(parts[2])) {
                if (parts.length == 5 && "active".equals(parts[4])) {
                    handleActiveProductByHash(parts[3], request, response);
                } else if (parts.length == 4) {
                    handleProductByHash(parts[3], request, response);
                } else if (parts.length == 3) {
                    if ("true".equalsIgnoreCase(request.getParameter("abaixo-estoque-min"))) {
                        handleProductsBelowMinStock(request, response);
                    } else {
                        handleAllProducts(request, response);
                    }
                } else {
                    sendErrorResponse(response, "Invalid URL");
                }
            } else {
                sendErrorResponse(response, "Resource not found");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleActiveProductByHash(String hash, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProductReturnDTO product = productService.getActiveProductByHash(UUID.fromString(hash));
        if (product != null) {
            sendJsonResponse(response, product);
        } else {
            sendErrorResponse(response, messages.getString("error.inactiveOrNotFound"));
        }
    }

    private void handleProductByHash(String hash, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Product product = productService.getProductByHash(UUID.fromString(hash));
        if (product != null) {
            sendJsonResponse(response, product);
        } else {
            sendErrorResponse(response, messages.getString("error.notFound"));
        }
    }

    private void handleProductsBelowMinStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ProductReturnDTO> productsBelowMinStock = productService.getProductsBelowMinStock();
        if (productsBelowMinStock != null) {
            sendJsonResponse(response, productsBelowMinStock);
        } else {
            sendErrorResponse(response, messages.getString("error.cannotRetrieveData"));
        }
    }

    private void handleAllProducts(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String activeParam = request.getParameter("active");
        boolean onlyActive = "true".equalsIgnoreCase(activeParam);
        boolean onlyInactive = "false".equalsIgnoreCase(activeParam);

        List<ProductReturnDTO> products = null;

        if (onlyActive) {
            products = productService.getActiveProducts();
        } else if (onlyInactive) {
            products = productService.getInactiveProducts();
        } else {
            products = productService.getAllProducts();
        }

        if (products != null) {
            sendJsonResponse(response, products);
        } else {
            sendErrorResponse(response, messages.getString("error.cannotRetrieveData"));
        }
    }



    //Método POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestURI = request.getRequestURI();

            if (requestURI.endsWith("/batch")) {
                processBatchCreation(request, response);
            } else if (requestURI.endsWith("/batch-price-update")) {
                processBatchPriceUpdate(request, response);
            } else if (requestURI.endsWith("/batch-quantity-update")) {
                processBatchQuantityUpdate(request, response);
            } else {
                processSingleProductCreation(request, response);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void processBatchCreation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Lê o JSON de entrada da solicitação HTTP
            String jsonInput = readJsonInput(request);

            // Faz o parsing do JSON em um array de ProductDTO usando Gson
            List<ProductDTO> productDTOs = parseJsonToProductDTOList(jsonInput);

            // Crie um objeto ProductBatchDTO e configure sua lista de produtos
            ProductBatchDTO batchDTO = new ProductBatchDTO();
            batchDTO.setProductDTOs(productDTOs);

            // Chama o método createProductsInBatch para processar o lote de produtos
            JsonObject result = productService.createProductsInBatch(batchDTO);

            // Verifica se há produtos com erros de validação
            if (result.has("products_with_errors")) {
                // Pelo menos um produto teve erro de validação, configure uma resposta de erro com status HTTP 400 (Bad Request)
                configureJsonResponse(response, result);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                // Todos os produtos foram criados com sucesso, configure uma resposta de sucesso
                configureJsonResponse(response, result);
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void processBatchPriceUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Ler o JSON de entrada da solicitação HTTP
            String jsonInput = readJsonInput(request);

            // Fazer o parsing do JSON em uma lista de ProductPriceUpdateDTO usando Gson
            ProductPriceUpdateDTO[] updates = parseJsonToProductPriceUpdateArray(jsonInput);

            // Chamar o método para atualizar os preços em lote
            List<JsonObject> produtosAtualizados = productService.updateProductPricesInBatch(Arrays.asList(updates));

            // Construir um objeto JSON para a resposta
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("produtosAtualizados", gson.toJsonTree(produtosAtualizados));

            // Configurar a resposta HTTP
            configureJsonResponse(response, jsonResponse);
        } catch (Exception e) {
            handleException(response, e);
        }
    }



    private void processBatchQuantityUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Ler o JSON de entrada da solicitação HTTP
            String jsonInput = readJsonInput(request);

            // Fazer o parsing do JSON em uma lista de ProductQuantityUpdateDTO usando Gson
            ProductQuantityUpdateDTO[] updates = parseJsonToProductQuantityUpdateArray(jsonInput);

            // Chamar o método para atualizar as quantidades em lote
            JsonObject result = productService.updateProductQuantitiesInBatch(updates);

            // Configurar a resposta HTTP
            configureJsonResponse(response, result);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void processSingleProductCreation(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Ler o JSON de entrada da solicitação HTTP
            String jsonInput = readJsonInput(request);

            // Fazer o parsing do JSON em um objeto ProductDTO usando Gson
            ProductDTO newProductDTO = parseJsonToProductDTO(jsonInput);

            // Criar um novo produto com base no DTO
            Product newProduct = createProductFromDTO(newProductDTO);

            // Chamar o método para criar o produto
            Product  createdProduct = productService.createProduct(newProduct); // Alteração aqui

            // Configurar a resposta HTTP
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", messages.getString("product.create.success"));
            responseJson.add("product", gson.toJsonTree(createdProduct)); // Incluir o produto criado na resposta

            configureJsonResponse(response, responseJson);
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private List<ProductDTO> parseJsonToProductDTOList(String jsonInput) {
        Type listType = new TypeToken<List<ProductDTO>>() {}.getType();
        return gson.fromJson(jsonInput, listType);
    }

    private ProductPriceUpdateDTO[] parseJsonToProductPriceUpdateArray(String jsonInput) {
        return gson.fromJson(jsonInput, ProductPriceUpdateDTO[].class);
    }

    private ProductQuantityUpdateDTO[] parseJsonToProductQuantityUpdateArray(String jsonInput) {
        return gson.fromJson(jsonInput, ProductQuantityUpdateDTO[].class);
    }

    private ProductDTO parseJsonToProductDTO(String jsonInput) {
        return gson.fromJson(jsonInput, ProductDTO.class);
    }

    private Product createProductFromDTO(ProductDTO productDTO) {
        return new Product(
                productDTO.getNome(),
                productDTO.getDescricao(),
                productDTO.getEan13(),
                productDTO.getPreco(),
                productDTO.getQuantidade(),
                productDTO.getEstoqueMin()
        );
    }



    // Método PUT
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestURI = request.getRequestURI();
            String[] parts = requestURI.split("/");

            if (parts.length != 4 || !"products".equals(parts[2])) {
                sendErrorResponse(response, messages.getString("product.invalid.url"));
                return;
            }

            String hash = parts[3];
            String jsonInput = readJsonInput(request);
            JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();

            ProductUpdateDTO updateDTO = gson.fromJson(jsonObject, ProductUpdateDTO.class);
            updateDTO.setHash(hash);

            Product updatedProduct = productService.updateProduct(updateDTO);

            if (updatedProduct != null) {
                JsonObject confirmation = new JsonObject();
                confirmation.addProperty("message", messages.getString("product.update.success"));
                confirmation.add("product", gson.toJsonTree(updatedProduct)); // Adicione o produto atualizado à resposta JSON

                sendJsonResponse(response, confirmation);
            } else {
                sendErrorResponse(response, messages.getString("product.update.error"));
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }



    // Método PATCH
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            String[] parts = requestURI.split("/");

            if (parts.length != 5 || !"products".equals(parts[2]) || !"activation".equals(parts[4])) {
                sendErrorResponse(response, messages.getString("product.invalid.url"));
                return;
            }

            String hash = parts[3];
            String jsonInput = readJsonInput(request);
            JsonObject jsonObject = JsonParser.parseString(jsonInput).getAsJsonObject();

            if (!jsonObject.has("lativo")) {
                sendErrorResponse(response, messages.getString("product.missing.field") + "lativo");
                return;
            }

            boolean isActive = jsonObject.get("lativo").getAsBoolean();
            UUID productHash = UUID.fromString(hash);
            boolean updated = productService.activateOrDeactivateProduct(productHash, isActive);

            JsonObject confirmation = new JsonObject();
            confirmation.addProperty("message", updated ? (isActive ? messages.getString("product.activate.success") : messages.getString("product.deactivate.success")) : messages.getString("product.update.error"));

            sendJsonResponse(response, confirmation);
        } catch (Exception e) {
            handleException(response, e);
        }
    }



    // Método DELETE
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String hash = request.getPathInfo().substring(1); // Remove a barra inicial
            UUID productHash = UUID.fromString(hash);

            ProductReturnDTO deletedProduct = productService.deleteProduct(productHash);

            JsonObject jsonResponse = new JsonObject();
            if (deletedProduct != null) {
                jsonResponse.addProperty("message", messages.getString("product.delete.success"));
                // Adicione o produto excluído ao corpo da resposta JSON
                jsonResponse.add("Produto Excluído", new Gson().toJsonTree(deletedProduct));
            } else {
                jsonResponse.addProperty("message", messages.getString("product.notfound"));
            }

            sendJsonResponse(response, jsonResponse);
        } catch (Exception e) {
            handleException(response, e);
        }
    }



    //Métodos auxiliares
    private void sendJsonResponse(HttpServletResponse response, Object responseObject) throws IOException {
        String jsonResponse = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("error", errorMessage);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(errorJson.toString());
        }
    }

    private String readJsonInput(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder jsonInput = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonInput.append(line);
        }
        return jsonInput.toString();
    }

    private void configureJsonResponse(HttpServletResponse response, JsonObject jsonResponse) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse.toString());
        }
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("message", e.getMessage());

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(errorJson));
        }
    }
}
