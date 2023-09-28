package Application.Servlet;

import Application.dto.ProductBatchDTO;
import Application.dto.ProductDTO;
import Domain.ProductService;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ResourceBundle;


@WebServlet("/products/batch/*")
public class ProductServletBatch extends HttpServlet {
    private ProductService productService = new ProductService();
    private Gson gson = new Gson();
    ResourceBundle messages = ResourceBundle.getBundle("messages");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
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
        } catch (Exception e) {
            // Em caso de exceção, lida com o erro e envia uma resposta de erro
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