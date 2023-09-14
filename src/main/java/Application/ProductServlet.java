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
        List<Product> products = productService.getAllProducts();

        // Converte a lista de produtos para JSON
        String jsonProducts = gson.toJson(products);

        // Configura a resposta para retornar JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Escreve a resposta JSON na saída
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonProducts);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lida com solicitações POST para criar um novo produto a partir de um JSON de entrada
        BufferedReader reader = request.getReader();
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

        // Converte o JSON para um objeto Product
        Product newProduct = gson.fromJson(jsonObject, Product.class);

        // Chame o método createProduct da ProductService para criar o produto
        productService.createProduct(newProduct);

        // Configura a resposta para retornar um JSON de confirmação
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Cria um JSON de confirmação simples
        JsonObject confirmation = new JsonObject();
        confirmation.addProperty("message",messages.getString("product.create.success"));

        // Escreve a resposta JSON na saída
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(confirmation));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lida com solicitações PUT para atualizar um produto existente a partir de um JSON de entrada
        BufferedReader reader = request.getReader();
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

        // Converte o JSON para um objeto Product
        Product updatedProduct = gson.fromJson(jsonObject, Product.class);

        // Certifique-se de que o hash do produto não seja nulo
        if (updatedProduct.getHash() == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(messages.getString("product.hash.required"));
            return;
        }

        // Chame o método updateProduct da ProductService para atualizar o produto
        boolean updated = productService.updateProduct(updatedProduct.getHash(), updatedProduct);

        // Configura a resposta para retornar um JSON de confirmação
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Cria um JSON de confirmação com base no resultado da atualização
        JsonObject confirmation = new JsonObject();
        if (updated) {
            confirmation.addProperty("message", messages.getString("product.update.success"));
        } else {
            confirmation.addProperty("message", messages.getString("product.update.error"));
        }

        // Escreve a resposta JSON na saída
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(confirmation));
        }
    }



    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lida com solicitações DELETE para excluir um produto existente a partir de um JSON de entrada
        BufferedReader reader = request.getReader();
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

        // Extrai o hash do produto a ser excluído do JSON
        UUID productHash = UUID.fromString(jsonObject.get("hash").getAsString());

        // Verifique se o produto com o hash especificado existe usando a instância de ProductDAO
        boolean productExists = productDAO.doesProductExist(productHash);

        // Configura a resposta para retornar um JSON de confirmação
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Cria um JSON de confirmação com base no resultado da exclusão
        JsonObject confirmation = new JsonObject();
        if (productExists) {
            productService.deleteProduct(productHash);
            confirmation.addProperty("message", messages.getString("product.delete.success"));
        } else {
            confirmation.addProperty("message",  messages.getString("product.notfound"));
        }

        // Escreve a resposta JSON na saída
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(confirmation));
        }
    }
}