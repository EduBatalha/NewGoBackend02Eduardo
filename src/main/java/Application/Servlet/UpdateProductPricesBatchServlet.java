package Application.Servlet;

import Application.dto.ProductPriceUpdateDTO;
import Domain.ProductService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/products/batch-price-update")
public class UpdateProductPricesBatchServlet extends HttpServlet {
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
        } catch (Exception e) {
            // Em caso de exceção, lidar com o erro e enviar uma resposta de erro
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", e.getMessage());
                out.print(errorResponse.toString());
            }
        }
    }


}
