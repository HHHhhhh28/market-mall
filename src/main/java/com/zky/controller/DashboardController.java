package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin/dashboard")
public class DashboardController {

    @Resource
    private OrderDao orderDao;
    
    @Resource
    private UserDao userDao;
    
    @Resource
    private ProductDao productDao;

    @GetMapping("/stats")
    public Response<DashboardVO> getDashboardStats() {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/dashboard/stats");
        
        DashboardVO vo = new DashboardVO();
        
        // 今日成交总额
        Double todayTotalAmount = orderDao.selectTodayTotalAmount();
        vo.setTodayTotalAmount(todayTotalAmount != null ? todayTotalAmount : 0.0);
        
        // 今日订单数
        Integer todayOrderCount = orderDao.selectTodayOrderCount();
        vo.setTodayOrderCount(todayOrderCount != null ? todayOrderCount : 0);
        
        // 累计用户数
        Integer totalUserCount = userDao.selectTotalUserCount();
        vo.setTotalUserCount(totalUserCount != null ? totalUserCount : 0);
        
        // 今日新增用户数
        Integer todayNewUserCount = userDao.selectTodayNewUserCount();
        vo.setTodayNewUserCount(todayNewUserCount != null ? todayNewUserCount : 0);
        
        // 商品总数
        Integer totalProductCount = productDao.selectTotalProductCount();
        vo.setTotalProductCount(totalProductCount != null ? totalProductCount : 0);
        
        // 近7天订单数趋势
        List<Map<String, Object>> orderTrendData = orderDao.selectOrderTrend();
        List<DashboardVO.OrderTrendVO> orderTrend = new ArrayList<>();
        if (orderTrendData != null) {
            for (Map<String, Object> item : orderTrendData) {
                DashboardVO.OrderTrendVO trend = new DashboardVO.OrderTrendVO();
                Object dateObj = item.get("date");
                if (dateObj != null) {
                    trend.setDate(dateObj.toString());
                }
                Object countObj = item.get("count");
                if (countObj != null) {
                    trend.setCount(Integer.parseInt(countObj.toString()));
                }
                orderTrend.add(trend);
            }
        }
        vo.setOrderTrend(orderTrend);
        
        // 用户城市分布
        List<Map<String, Object>> cityData = userDao.selectCityDistribution();
        List<DashboardVO.CityDistributionVO> cityDistribution = new ArrayList<>();
        if (cityData != null) {
            for (Map<String, Object> item : cityData) {
                DashboardVO.CityDistributionVO city = new DashboardVO.CityDistributionVO();
                Object cityObj = item.get("city");
                if (cityObj != null) {
                    city.setCity(cityObj.toString());
                }
                Object countObj = item.get("count");
                if (countObj != null) {
                    city.setCount(Integer.parseInt(countObj.toString()));
                }
                cityDistribution.add(city);
            }
        }
        vo.setCityDistribution(cityDistribution);
        
        // 销量TOP10商品
        List<Map<String, Object>> topProductsData = productDao.selectTopProducts();
        List<DashboardVO.ProductSalesVO> topProducts = new ArrayList<>();
        if (topProductsData != null) {
            for (Map<String, Object> item : topProductsData) {
                DashboardVO.ProductSalesVO product = new DashboardVO.ProductSalesVO();
                Object nameObj = item.get("productName");
                if (nameObj != null) {
                    product.setProductName(nameObj.toString());
                }
                Object salesObj = item.get("sales");
                if (salesObj != null) {
                    product.setSales(Integer.parseInt(salesObj.toString()));
                }
                topProducts.add(product);
            }
        }
        vo.setTopProducts(topProducts);
        
        // 库存预警商品
        List<Map<String, Object>> lowStockData = productDao.selectLowStockProducts(50);
        List<DashboardVO.LowStockProductVO> lowStockProducts = new ArrayList<>();
        if (lowStockData != null) {
            for (Map<String, Object> item : lowStockData) {
                DashboardVO.LowStockProductVO product = new DashboardVO.LowStockProductVO();
                Object productIdObj = item.get("product_id");
                if (productIdObj != null) {
                    product.setProductId(productIdObj.toString());
                }
                Object nameObj = item.get("name");
                if (nameObj != null) {
                    product.setName(nameObj.toString());
                }
                Object stockObj = item.get("stock");
                if (stockObj != null) {
                    product.setStock(Integer.parseInt(stockObj.toString()));
                }
                Object categoryObj = item.get("category");
                if (categoryObj != null) {
                    product.setCategory(categoryObj.toString());
                }
                Object priceObj = item.get("price");
                if (priceObj != null) {
                    product.setPrice(new java.math.BigDecimal(priceObj.toString()));
                }
                lowStockProducts.add(product);
            }
        }
        vo.setLowStockProducts(lowStockProducts);
        
        return Response.<DashboardVO>builder().code("0000").info("Success").data(vo).build();
    }
}
