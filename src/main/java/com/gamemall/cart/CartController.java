package com.gamemall.cart;

import com.gamemall.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ApiResponse<Void> add(@Validated @RequestBody CartRequest request) {
        cartService.add(request);
        return ApiResponse.ok(null);
    }

    @GetMapping
    public ApiResponse<List<CartView>> list() {
        return ApiResponse.ok(cartService.list());
    }

    @PutMapping("/{gameId}")
    public ApiResponse<Void> update(@PathVariable Long gameId, @RequestParam int quantity) {
        cartService.update(gameId, quantity);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{gameId}")
    public ApiResponse<Void> delete(@PathVariable Long gameId) {
        cartService.delete(gameId);
        return ApiResponse.ok(null);
    }
}
