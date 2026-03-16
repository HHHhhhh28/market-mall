package com.zky.domain.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private Double todayTotalAmount;
    private Integer todayOrderCount;
    private Integer totalUserCount;
    private Integer todayNewUserCount;
    private Integer totalProductCount;
    private List<OrderTrendVO> orderTrend;
    private List<CityDistributionVO> cityDistribution;
    private List<ProductSalesVO> topProducts;
    private List<LowStockProductVO> lowStockProducts;
    
    @Data
    public static class OrderTrendVO {
        private String date;
        private Integer count;
    }
    
    @Data
    public static class CityDistributionVO {
        private String city;
        private Integer count;
    }
    
    @Data
    public static class ProductSalesVO {
        private String productName;
        private Integer sales;
    }
    
    @Data
    public static class LowStockProductVO {
        private String productId;
        private String name;
        private Integer stock;
        private String category;
        private java.math.BigDecimal price;
    }
}
