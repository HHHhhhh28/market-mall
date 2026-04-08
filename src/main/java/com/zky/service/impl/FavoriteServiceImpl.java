package com.zky.service.impl;

import com.zky.dao.FavoriteDao;
import com.zky.dao.ProductDao;
import com.zky.domain.dto.FavoriteInfoRequestDTO;
import com.zky.domain.po.FavoriteInfo;
import com.zky.domain.po.ProductInfo;
import com.zky.service.IFavoriteService;
import com.zky.domain.vo.FavoriteVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements IFavoriteService {

    @Resource
    private FavoriteDao favoriteDao;
    @Resource
    private ProductDao productDao;

    @Override
    public void addFavorite(FavoriteInfoRequestDTO request) {
        if (favoriteDao.selectOne(request.getUserId(), request.getProductId()) != null) {
            return; // 已收藏
        }
        FavoriteInfo favorite = new FavoriteInfo();
        favorite.setFavoriteId(UUID.randomUUID().toString());
        favorite.setUserId(request.getUserId());
        favorite.setProductId(request.getProductId());
        favorite.setCreateTime(new Date());
        favoriteDao.insert(favorite);
    }

    @Override
    public void removeFavorite(String userId, String productId) {
        favoriteDao.delete(userId, productId);
    }

    @Override
    public List<FavoriteVO> getFavoriteList(String userId) {
        List<FavoriteInfo> favorites = favoriteDao.selectByUserId(userId);
        return favorites.stream().map(f -> {
            ProductInfo p = productDao.selectByProductId(f.getProductId());
            FavoriteVO vo = new FavoriteVO();
            vo.setFavoriteId(f.getFavoriteId());
            vo.setProductId(f.getProductId());
            vo.setFavoriteCount(favoriteDao.countByProductId(f.getProductId()));
            if (p != null) {
                vo.setProductName(p.getName());
                vo.setProductImage(p.getImageUrl());
                vo.setPrice(p.getPrice());
                vo.setCategory(p.getCategory());
            }
            return vo;
        }).collect(Collectors.toList());
    }
}
