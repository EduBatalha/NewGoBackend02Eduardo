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
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    sendErrorResponse(response, "Invalid URL");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                sendErrorResponse(response, "Resource not found");
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleActiveProductByHash(String hash, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Product product = productService.getActiveProductByHash(UUID.fromString(hash));
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
        List<Product> productsBelowMinStock = productService.getProductsBelowMinStock();
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

        List<Product> products = null;

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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String requestURI = request.getRequestURI();

            if (requestURI.endsWith("/batch")) {
                // É um lote de produtos

                // Lê o JSON de entrada da solicitação HTTP
                BufferedReader reader = request.getReader();
                StringBuilder jsonInput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonInput.append(line);
                }

                // Faz o parsing do JSON em um array de ProductDTO usando Gson
                Type listType = new TypeToken<List<ProductDTO>>() {}.getType();
                List<ProductDTO> productDTOs = gson.fromJson(jsonInput.toString(), listType);

                // Crie um objeto ProductBatchDTO e configure sua lista de produtos
                ProductBatchDTO batchDTO = new ProductBatchDTO();
                batchDTO.setProductDTOs(productDTOs);

                // Chama o método createProductsInBatch para processar o lote de produtos
                JsonObject result = productService.createProductsInBatch(batchDTO);

                // Configura a resposta HTTP
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // Escreve o resultado JSON como resposta
                try (PrintWriter out = response.getWriter()) {
                    out.print(result.toString());
                }
            } else if (requestURI.endsWith("/batch-price-update")) {
                // É uma atualização de preços em lote

                // Ler o JSON de entrada da solicitação HTTP
                BufferedReader reader = request.getReader();
                StringBuilder jsonInput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonInput.append(line);
                }

                // Fazer o parsing do JSON em uma lista de ProductPriceUpdateDTO usando Gson
                ProductPriceUpdateDTO[] updates = gson.fromJson(jsonInput.toString(), ProductPriceUpdateDTO[].class);

                // Chamar o método para atualizar os preços em lote
                Map<String, Object> batchResult = productService.updateProductPricesInBatch(Arrays.asList(updates));
                List<String> erroProdutos = (List<String>) batchResult.get("erroProdutos");
                List<String> produtosAtualizados = (List<String>) batchResult.get("produtosAtualizados");

                // Configurar a resposta HTTP
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // Construir um objeto JSON para a resposta
                JsonObject jsonResponse = new JsonObject();

                if (!erroProdutos.isEmpty()) {
                    jsonResponse.addProperty("error", String.join(" - ", erroProdutos));
                }

                if (!produtosAtualizados.isEmpty()) {
                    jsonResponse.addProperty("message", String.join(" - ", produtosAtualizados));
                }

                try (PrintWriter out = response.getWriter()) {
                    out.print(jsonResponse.toString());
                }
            } else if (requestURI.endsWith("/batch-quantity-update")) {
                // É uma atualização de quantidade em lote

                // Ler o JSON de entrada da solicitação HTTP
                BufferedReader reader = request.getReader();
                StringBuilder jsonInput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonInput.append(line);
                }

                // Fazer o parsing do JSON em uma lista de ProductQuantityUpdateDTO usando Gson
                ProductQuantityUpdateDTO[] updates = gson.fromJson(jsonInput.toString(), ProductQuantityUpdateDTO[].class);

                // Chamar o método para atualizar as quantidades em lote
                JsonObject result = productService.updateProductQuantitiesInBatch(updates);

                // Configurar a resposta HTTP
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                // Escrever a resposta JSON
                try (PrintWriter out = response.getWriter()) {
                    out.print(result.toString());
                }
            } else {
                // É a criação de um único produto

                BufferedReader reader = request.getReader();
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                ProductDTO newProductDTO = gson.fromJson(jsonObject, ProductDTO.class);

                Product newProduct = new Product(
                        newProductDTO.getNome(),
                        newProductDTO.getDescricao(),
                        newProductDTO.getEan13(),
                        newProductDTO.getPreco(),
                        newProductDTO.getQuantidade(),
                        newProductDTO.getEstoqueMin()
                );

                productService.createProduct(newProduct);

                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                JsonObject confirmation = new JsonObject();
                confirmation.addProperty("message", messages.getString("product.create.success"));

                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(confirmation));
                }
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Obtenha o hash da URL
            String requestURI = request.getRequestURI();
            String[] parts = requestURI.split("/");

            if (parts.length != 4 || !"products".equals(parts[2])) {
                // URL inválida, retorne um erro
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", messages.getString("product.invalid.url"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(errorJson));
                return;
            }

            String hash = parts[3];

            // Verifique se o DTO contém apenas os campos desejados
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            // Crie um objeto ProductUpdateDTO com base no JSON
            ProductUpdateDTO updateDTO = gson.fromJson(jsonObject, ProductUpdateDTO.class);

            // Configure a hash no DTO
            updateDTO.setHash(hash);

            // Chame o serviço para atualizar o produto
            boolean updated = productService.updateProduct(updateDTO);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JsonObject confirmation = new JsonObject();
            if (updated) {
                confirmation.addProperty("message", messages.getString("product.update.success"));
            } else {
                confirmation.addProperty("message", messages.getString("product.update.error"));
            }

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(confirmation));
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }


    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Obtenha o hash da URL
            String requestURI = request.getRequestURI();
            String[] parts = requestURI.split("/");

            if (parts.length != 4 || !"products".equals(parts[2])) {
                // URL inválida, retorne um erro
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", messages.getString("product.invalid.url"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(errorJson));
                return;
            }

            String hash = parts[3];

            // Verifique se o DTO contém apenas o campo "lativo"
            BufferedReader reader = request.getReader();
            String line;
            StringBuilder jsonBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            String jsonContent = jsonBuilder.toString();

            if (jsonContent.isEmpty()) {
                // Corpo da solicitação vazio, retorne um erro
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", messages.getString("product.missing.field")+ String.join(", ", "lativo"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(errorJson));
                return;
            }

            JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();


            // Obtenha o valor do campo "lativo" do JSON
            boolean isActive = jsonObject.get("lativo").getAsBoolean();

            // Ative ou desative o produto com base no valor do campo "lativo"
            UUID productHash = UUID.fromString(hash);
            boolean updated = productService.activateOrDeactivateProduct(productHash, isActive);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JsonObject confirmation = new JsonObject();
            if (updated) {
                confirmation.addProperty("message", isActive ? messages.getString("product.activate.success") : messages.getString("product.deactivate.success"));
            } else {
                confirmation.addProperty("message", messages.getString("product.update.error"));
            }

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(confirmation));
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // Obtenha o hash da URL
            String hash = request.getPathInfo(); // Isso conterá "/hash" (por exemplo, "/609af184-6347-4226-a596-b27796944491")

            // Remova a barra inicial
            hash = hash.substring(1);

            UUID productHash = UUID.fromString(hash);

            boolean productExists = productDAO.doesProductExist(productHash);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JsonObject confirmation = new JsonObject();
            if (productExists) {
                productService.deleteProduct(productHash);
                confirmation.addProperty("message", messages.getString("product.delete.success"));
            } else {
                confirmation.addProperty("message", messages.getString("product.notfound"));
            }

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(confirmation));
            }
        } catch (Exception e) {
            handleException(response, e);
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
