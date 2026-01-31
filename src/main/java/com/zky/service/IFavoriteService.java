package com.zky.service;

import com.zky.domain.dto.FavoriteInfoRequestDTO;
import com.zky.domain.vo.FavoriteVO;

import java.util.List;

public interface IFavoriteService {
    void addFavorite(FavoriteInfoRequestDTO request);
    void removeFavorite(String userId, String productId);
    List<FavoriteVO> getFavoriteList(String userId);
}
