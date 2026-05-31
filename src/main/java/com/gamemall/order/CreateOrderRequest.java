package com.gamemall.order;

import java.util.List;

public class CreateOrderRequest {
    public List<OrderLineRequest> items;
    public Boolean fromCart = true;
}
