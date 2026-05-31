package com.gamemall.cart;

import com.gamemall.common.BizException;
import com.gamemall.game.Game;
import com.gamemall.game.GameMapper;
import com.gamemall.security.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private final CartMapper cartMapper;
    private final GameMapper gameMapper;

    public CartService(CartMapper cartMapper, GameMapper gameMapper) {
        this.cartMapper = cartMapper;
        this.gameMapper = gameMapper;
    }

    @Transactional
    public void add(CartRequest request) {
        Long userId = SecurityContext.currentUserId();
        Game game = gameMapper.findOnlineById(request.gameId);
        if (game == null) {
            throw new BizException(404, "game not found");
        }
        if (game.getStock() < request.quantity) {
            throw new BizException("insufficient stock");
        }
        CartItem existing = cartMapper.find(userId, request.gameId);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setGameId(request.gameId);
            item.setQuantity(request.quantity);
            cartMapper.insert(item);
        } else {
            int quantity = existing.getQuantity() + request.quantity;
            if (game.getStock() < quantity) {
                throw new BizException("insufficient stock");
            }
            cartMapper.updateQuantity(userId, request.gameId, quantity);
        }
    }

    public List<CartView> list() {
        return cartMapper.list(SecurityContext.currentUserId());
    }

    @Transactional
    public void update(Long gameId, int quantity) {
        if (quantity < 1) {
            throw new BizException("quantity must be positive");
        }
        cartMapper.updateQuantity(SecurityContext.currentUserId(), gameId, quantity);
    }

    @Transactional
    public void delete(Long gameId) {
        cartMapper.delete(SecurityContext.currentUserId(), gameId);
    }

    @Transactional
    public void clear(Long userId) {
        cartMapper.clear(userId);
    }
}
