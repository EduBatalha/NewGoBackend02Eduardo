package Controllers;

import com.google.gson.Gson;
import Data.Product;
import Service.ProductService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private ProductService productService = new ProductService();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Product> products = productService.getAllProducts();
        String jsonProducts = gson.toJson(products);
        response.setContentType("application/json");
        response.getWriter().write(jsonProducts);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        Product newProduct = gson.fromJson(reader, Product.class);
        productService.createProduct(newProduct);
        response.setStatus(HttpServletResponse.SC_CREATED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obtém o ID do produto a ser atualizado dos parâmetros da solicitação
        String productIdStr = request.getParameter("id");

        if (productIdStr != null && !productIdStr.isEmpty()) {
            try {
                // Converte o ID para o tipo correto (neste caso, long)
                long productId = Long.parseLong(productIdStr);

                // Lê o JSON do corpo da solicitação e converte em um objeto Product
                BufferedReader reader = request.getReader();
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                Product updatedProduct = gson.fromJson(requestBody.toString(), Product.class);

                // Chame o serviço para atualizar o produto
                boolean updated = productService.updateProduct(productId, updatedProduct);

                if (updated) {
                    response.setStatus(HttpServletResponse.SC_OK); // 200 OK
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found (Produto não encontrado)
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request (ID do produto inválido)
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request (ID do produto ausente ou inválido)
        }
    }





    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int productId = Integer.parseInt(request.getParameter("id"));
        productService.deleteProduct(productId);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}

