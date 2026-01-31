package com.zky.service;

import com.zky.domain.dto.CartInfoRequestDTO;
import com.zky.domain.vo.CartVO;

public interface ICartService {
    void addCart(CartInfoRequestDTO request);
    void updateCart(CartInfoRequestDTO request);
    void removeCart(String userId, String productId);
    void clearCart(String userId);
    CartVO getCart(String userId);
}
