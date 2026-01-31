package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.FavoriteInfoRequestDTO;
import com.zky.service.IFavoriteService;
import com.zky.domain.vo.FavoriteVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/favorite")
public class FavoriteController {

    @Resource
    private IFavoriteService favoriteService;

    @PostMapping("/add")
    public Response<Void> addFavorite(@RequestBody FavoriteInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/favorite/add");
        favoriteService.addFavorite(request);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @DeleteMapping("/remove")
    public Response<Void> removeFavorite(@RequestParam String userId, @RequestParam String productId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/favorite/remove");
        favoriteService.removeFavorite(userId, productId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @GetMapping("/list")
    public Response<List<FavoriteVO>> getFavoriteList(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/favorite/list");
        List<FavoriteVO> list = favoriteService.getFavoriteList(userId);
        return Response.<List<FavoriteVO>>builder().code("0000").info("Success").data(list).build();
    }
}
