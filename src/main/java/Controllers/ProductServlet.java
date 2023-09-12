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
        BufferedReader reader = request.getReader();
        Product updatedProduct = gson.fromJson(reader, Product.class);
        productService.updateProduct(updatedProduct);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long productId = Long.parseLong(request.getParameter("id"));
        productService.deleteProduct(productId);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}

