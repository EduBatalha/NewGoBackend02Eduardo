package Application.Servlet;

import Application.dto.ProductDTO;
import Application.dto.ProductUpdateDTO;
import Infrastructure.Entity.Product;
import Infrastructure.dao.ProductDAO;
import Domain.ProductService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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

            if (parts.length == 5 && "products".equals(parts[2]) && "active".equals(parts[4])) {
                String hash = parts[3];

                Product product = productService.getActiveProductByHash(UUID.fromString(hash));

                if (product != null) {
                    String jsonProduct = gson.toJson(product);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    try (PrintWriter out = response.getWriter()) {
                        out.print(jsonProduct);
                    }
                } else {
                    // Produto não encontrado ou não está ativo
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject errorJson = new JsonObject();
                    errorJson.addProperty("error", messages.getString("error.inactiveOrNotFound"));
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(errorJson.toString());
                    }
                }
            } else if (parts.length == 4 && "products".equals(parts[2])) {
                String hash = parts[3];

                Product product = productService.getProductByHash(UUID.fromString(hash));

                if (product != null) {
                    String jsonProduct = gson.toJson(product);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    try (PrintWriter out = response.getWriter()) {
                        out.print(jsonProduct);
                    }
                } else {
                    // Produto não encontrado
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    JsonObject errorJson = new JsonObject();
                    errorJson.addProperty("error", messages.getString("error.notFound"));
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(errorJson.toString());
                    }
                }
            } else {
                // Consulta todos os produtos ou lida com outras consultas de produtos aqui, se necessário
                String activeParam = request.getParameter("active");
                boolean onlyActive = activeParam != null && activeParam.equalsIgnoreCase("true");
                boolean onlyInactive = activeParam != null && activeParam.equalsIgnoreCase("false");

                List<Product> products = null;

                if (onlyActive) {
                    // Consulta apenas produtos ativos
                    products = productService.getActiveProducts();
                } else if (onlyInactive) {
                    // Consulta apenas produtos inativos
                    products = productService.getInactiveProducts();
                } else {
                    // Consulta todos os produtos
                    products = productService.getAllProducts();
                }

                if (products != null) {
                    String jsonProducts = gson.toJson(products);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    try (PrintWriter out = response.getWriter()) {
                        out.print(jsonProducts);
                    }
                } else {
                    // Não foi possível recuperar os produtos
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    JsonObject errorJson = new JsonObject();
                    errorJson.addProperty("error", messages.getString("error.cannotRetrieveData"));
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    try (PrintWriter out = response.getWriter()) {
                        out.print(errorJson.toString());
                    }
                }
            }
        } catch (Exception e) {
            handleException(response, e);
        }
    }




    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            ProductDTO newProductDTO = gson.fromJson(jsonObject, ProductDTO.class);

            // Converta o ProductDTO para um objeto Product
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
        } catch (Exception e) {
            handleException(response, e);
        }
    }

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

            // Encontre os campos não desejados do JSON e colete os nomes
            Set<String> allowedFields = new HashSet<>(Arrays.asList("descricao", "preco", "quantidade", "estoque_min"));
            Set<String> jsonFields = jsonObject.keySet();
            List<String> removedFields = new ArrayList<>();
            jsonFields.removeIf(field -> {
                if (!allowedFields.contains(field)) {
                    removedFields.add(field);
                    return true;
                }
                return false;
            });

            // Se campos não desejados forem encontrados, retorne uma mensagem
            if (!removedFields.isEmpty()) {
                JsonObject removedFieldsJson = new JsonObject();
                removedFieldsJson.addProperty("error", messages.getString("product.invalid.field") + (": ") + String.join(", ", removedFields));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(removedFieldsJson));
                return;
            }


            // Crie um objeto ProductUpdateDTO com base no JSON
            ProductUpdateDTO updateDTO = gson.fromJson(jsonObject, ProductUpdateDTO.class);

            // Configure a hash no DTO
            updateDTO.setHash(hash);

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
                // Campos obrigatórios ausentes, retorne um erro no corpo JSON
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", messages.getString("product.missing.field")+ String.join(", ", missingFields));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(errorJson));
                return;
            }


            // Crie um objeto Product com base no DTO
            Product updatedProduct = new Product();
            updatedProduct.setHash(UUID.fromString(updateDTO.getHash()));

            // Define os campos a serem atualizados no objeto Product com base no DTO
            updatedProduct.setDescription(updateDTO.getDescricao());
            updatedProduct.setPrice(updateDTO.getPreco());
            updatedProduct.setQuantity(updateDTO.getQuantidade());
            updatedProduct.setMinStock(updateDTO.getEstoqueMin());

            // Atualize o produto usando o ProductService
            boolean updated = productService.updateProduct(updatedProduct.getHash(), updatedProduct);

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
        //TODO
    }

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
