package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.OrderInfoRequestDTO;
import com.zky.service.IOrderService;
import com.zky.domain.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/order")
public class OrderController {

    @Resource
    private IOrderService orderService;

    @PostMapping("/create")
    public Response<String> createOrder(@RequestBody OrderInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/order/create");
        String orderId = orderService.createOrder(request);
        return Response.<String>builder().code("0000").info("Success").data(orderId).build();
    }

    @GetMapping("/list")
    public Response<List<OrderVO>> getOrderList(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/order/list");
        List<OrderVO> list = orderService.getOrderList(userId);
        return Response.<List<OrderVO>>builder().code("0000").info("Success").data(list).build();
    }

    @GetMapping("/detail/{orderId}")
    public Response<OrderVO> getOrderDetail(@PathVariable String orderId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/order/detail/" + orderId);
        OrderVO vo = orderService.getOrderDetail(orderId);
        return Response.<OrderVO>builder().code("0000").info("Success").data(vo).build();
    }
}
