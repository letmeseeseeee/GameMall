package com.gamemall.order;

import com.gamemall.common.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetail> create(@RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @GetMapping
    public ApiResponse<List<Order>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(orderService.list(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderDetail> pay(@PathVariable Long id) {
        return ApiResponse.ok(orderService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ApiResponse.ok(null);
    }
}
