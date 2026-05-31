package com.gamemall.cart;

import java.math.BigDecimal;

public class CartView {
    public Long id;
    public Long gameId;
    public String title;
    public String coverUrl;
    public BigDecimal price;
    public Integer stock;
    public Integer quantity;
    public BigDecimal subtotal;
}
