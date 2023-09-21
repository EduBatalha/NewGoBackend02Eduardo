package Application;

import Application.DTO.ProductDTO;
import Application.DTO.ProductUpdateDTO;
import Infrastructure.Product;
import Infrastructure.DAO.ProductDAO;
import Domain.ProductService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private ProductService productService = new ProductService();
    private ProductDAO productDAO = new ProductDAO();
    ResourceBundle messages = ResourceBundle.getBundle("messages");
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Product> products = productService.getAllProducts();
            String jsonProducts = gson.toJson(products);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.print(jsonProducts);
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
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();


            // Converta o JSON em um objeto ProductUpdateDTO
            ProductUpdateDTO updateDTO = gson.fromJson(jsonObject, ProductUpdateDTO.class);

            // Obtém o productHash do DTO
            String productHashStr = updateDTO.getHash();
            UUID productHash = UUID.fromString(productHashStr);

            // Verifique se o DTO contém apenas os campos desejados
            if (productHash == null || updateDTO.getDescricao() == null ||
                    updateDTO.getPreco() <= 0 || updateDTO.getQuantidade() <= 0 ||
                    updateDTO.getEstoqueMin() <= 0) {
                // Campos inválidos ou ausentes, retorne um erro no corpo JSON
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", messages.getString("product.invalid.field"));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(gson.toJson(errorJson));
                return;
            }

            // Crie um objeto Product com o hash a partir do DTO
            Product updatedProduct = new Product();
            updatedProduct.setHash(productHash);

            // Define os campos a serem atualizados no objeto Product com base no DTO
            updatedProduct.setDescription(updateDTO.getDescricao());
            updatedProduct.setPrice(updateDTO.getPreco());
            updatedProduct.setQuantity(updateDTO.getQuantidade());
            updatedProduct.setMinStock(updateDTO.getEstoqueMin());

            // Atualize o produto usando o ProductService
            boolean updated = productService.updateProduct(productHash, updatedProduct);

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





    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            UUID productHash = UUID.fromString(jsonObject.get("hash").getAsString());

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
