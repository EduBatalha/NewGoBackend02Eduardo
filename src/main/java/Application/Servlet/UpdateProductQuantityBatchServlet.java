package Application.Servlet;


import Application.dto.ProductQuantityUpdateDTO;
import Domain.ProductService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/products/batch-quantity-update")
public class UpdateProductQuantityBatchServlet extends HttpServlet {
    private ProductService productService = new ProductService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
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
        } catch (Exception e) {
            // Em caso de exceção, lidar com o erro e enviar uma resposta de erro
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }



}
