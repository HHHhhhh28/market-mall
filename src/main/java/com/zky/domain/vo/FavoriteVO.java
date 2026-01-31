package com.zky.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class FavoriteVO {
    private String favoriteId;
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
}
