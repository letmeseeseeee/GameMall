package com.gamemall.order;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class OrderLineRequest {
    @NotNull
    public Long gameId;
    @NotNull
    @Min(1)
    public Integer quantity;
}
