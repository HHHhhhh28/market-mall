package com.zky.service.impl;

import com.zky.dao.CartDao;
import com.zky.dao.ProductDao;
import com.zky.domain.dto.CartInfoRequestDTO;
import com.zky.domain.po.CartInfo;
import com.zky.domain.po.ProductInfo;
import com.zky.service.ICartService;
import com.zky.domain.vo.CartItemVO;
import com.zky.domain.vo.CartVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {

    @Resource
    private CartDao cartDao;
    @Resource
    private ProductDao productDao;

    @Override
    public void addCart(CartInfoRequestDTO request) {
        CartInfo existing = cartDao.selectOne(request.getUserId(), request.getProductId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.setUpdateTime(new Date());
            cartDao.update(existing);
        } else {
            CartInfo cart = new CartInfo();
            cart.setUserId(request.getUserId());
            cart.setProductId(request.getProductId());
            cart.setQuantity(request.getQuantity());
            cart.setCreateTime(new Date());
            cart.setUpdateTime(new Date());
            cartDao.insert(cart);
        }
    }

    @Override
    public void updateCart(CartInfoRequestDTO request) {
        CartInfo existing = cartDao.selectOne(request.getUserId(), request.getProductId());
        if (existing != null) {
            existing.setQuantity(request.getQuantity());
            existing.setUpdateTime(new Date());
            cartDao.update(existing);
        }
    }

    @Override
    public void removeCart(String userId, String productId) {
        cartDao.delete(userId, productId);
    }

    @Override
    public void clearCart(String userId) {
        cartDao.deleteByUserId(userId);
    }

    @Override
    public CartVO getCart(String userId) {
        List<CartInfo> carts = cartDao.selectByUserId(userId);
        CartVO vo = new CartVO();
        
        List<CartItemVO> items = carts.stream().map(c -> {
            ProductInfo p = productDao.selectByProductId(c.getProductId());
            CartItemVO item = new CartItemVO();
            item.setProductId(c.getProductId());
            item.setQuantity(c.getQuantity());
            if (p != null) {
                item.setProductName(p.getName());
                item.setPrice(p.getPrice());
                item.setImageUrl(p.getImageUrl());
                item.setTotalPrice(p.getPrice().multiply(new BigDecimal(c.getQuantity())));
            } else {
                item.setProductName("未知商品");
                item.setPrice(BigDecimal.ZERO);
                item.setTotalPrice(BigDecimal.ZERO);
            }
            return item;
        }).collect(Collectors.toList());

        vo.setItems(items);
        //计算购物车商品总数量
        vo.setTotalQuantity(items.stream().mapToInt(CartItemVO::getQuantity).sum());
        //计算购物车商品总价格
        vo.setTotalPrice(items.stream().map(CartItemVO::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return vo;
    }
}
