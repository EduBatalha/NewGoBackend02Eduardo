package Application.dto;

import java.util.List;

public class ProductBatchDTO {
    private List<ProductDTO> productDTOs;


    public List<ProductDTO> getProductDTOs() {
        return productDTOs;
    }

    public void setProductDTOs(List<ProductDTO> productDTOs) {
        this.productDTOs = productDTOs;
    }
}
