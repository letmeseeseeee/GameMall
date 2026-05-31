package com.gamemall.cart;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CartRequest {
    @NotNull
    public Long gameId;
    @NotNull
    @Min(1)
    public Integer quantity;
}
