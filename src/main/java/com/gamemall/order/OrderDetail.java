package com.gamemall.order;

import java.util.List;

public class OrderDetail {
    public Order order;
    public List<OrderItem> items;

    public OrderDetail(Order order, List<OrderItem> items) {
        this.order = order;
        this.items = items;
    }
}
