package Application;

import Data.Product;
import Data.DAO.ProductDAO;
import Service.ProductService;

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
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

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
            Product newProduct = gson.fromJson(jsonObject, Product.class);

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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            Product updatedProduct = gson.fromJson(jsonObject, Product.class);

            if (updatedProduct.getHash() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(messages.getString("product.hash.required"));
                return;
            }

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
