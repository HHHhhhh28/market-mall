package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.CartInfoRequestDTO;
import com.zky.service.ICartService;
import com.zky.domain.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/cart")
public class CartController {

    @Resource
    private ICartService cartService;

    @PostMapping("/add")
    public Response<Void> addCart(@RequestBody CartInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/cart/add");
        cartService.addCart(request);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @PostMapping("/update")
    public Response<Void> updateCart(@RequestBody CartInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/cart/update");
        cartService.updateCart(request);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @DeleteMapping("/remove")
    public Response<Void> removeCart(@RequestParam String userId, @RequestParam String productId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/cart/remove");
        cartService.removeCart(userId, productId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @DeleteMapping("/clear")
    public Response<Void> clearCart(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/cart/clear");
        cartService.clearCart(userId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @GetMapping("/list")
    public Response<CartVO> getCart(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/cart/list");
        CartVO vo = cartService.getCart(userId);
        return Response.<CartVO>builder().code("0000").info("Success").data(vo).build();
    }
}
