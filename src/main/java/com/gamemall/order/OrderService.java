package com.gamemall.order;

import com.gamemall.cart.CartService;
import com.gamemall.cart.CartView;
import com.gamemall.common.BizException;
import com.gamemall.game.Game;
import com.gamemall.game.GameMapper;
import com.gamemall.security.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final GameMapper gameMapper;
    private final CartService cartService;
    private final long timeoutMinutes;

    public OrderService(OrderMapper orderMapper, OrderItemMapper orderItemMapper, GameMapper gameMapper, CartService cartService,
                        @Value("${gamemall.order.timeout-minutes}") long timeoutMinutes) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.gameMapper = gameMapper;
        this.cartService = cartService;
        this.timeoutMinutes = timeoutMinutes;
    }

    @Transactional
    public OrderDetail create(CreateOrderRequest request) {
        Long userId = SecurityContext.currentUserId();
        List<OrderLineRequest> lines = resolveLines(request);
        if (CollectionUtils.isEmpty(lines)) {
            throw new BizException("order items cannot be empty");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineRequest line : lines) {
            Game game = gameMapper.findOnlineById(line.gameId);
            if (game == null) {
                throw new BizException(404, "game not found: " + line.gameId);
            }
            if (gameMapper.decreaseStock(line.gameId, line.quantity) == 0) {
                throw new BizException("insufficient stock: " + game.getTitle());
            }
            BigDecimal subtotal = game.getPrice().multiply(BigDecimal.valueOf(line.quantity));
            OrderItem item = new OrderItem();
            item.setGameId(line.gameId);
            item.setGameTitle(game.getTitle());
            item.setPrice(game.getPrice());
            item.setQuantity(line.quantity);
            item.setSubtotal(subtotal);
            items.add(item);
            total = total.add(subtotal);
        }

        Order order = new Order();
        order.setOrderNo(newOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING_PAYMENT.code());
        order.setPaymentStatus(PaymentStatus.UNPAID.code());
        order.setExpireAt(LocalDateTime.now().plusMinutes(timeoutMinutes));
        orderMapper.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }
        if (Boolean.TRUE.equals(request.fromCart)) {
            cartService.clear(userId);
        }
        return new OrderDetail(order, items);
    }

    public List<Order> list(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        return orderMapper.listByUser(SecurityContext.currentUserId(), (safePage - 1) * safeSize, safeSize);
    }

    public OrderDetail detail(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(SecurityContext.currentUserId())) {
            throw new BizException(404, "order not found");
        }
        return new OrderDetail(order, orderItemMapper.findByOrderId(order.getId()));
    }

    @Transactional
    public OrderDetail pay(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(SecurityContext.currentUserId())) {
            throw new BizException(404, "order not found");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.code())) {
            throw new BizException("order status cannot be paid");
        }
        if (order.getExpireAt().isBefore(LocalDateTime.now())) {
            cancelInternal(order);
            throw new BizException("order expired");
        }
        if (orderMapper.markPaid(order.getId()) == 0) {
            throw new BizException("payment status conflict");
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        for (OrderItem item : items) {
            gameMapper.increaseSold(item.getGameId(), item.getQuantity());
        }
        return new OrderDetail(orderMapper.findById(id), items);
    }

    @Transactional
    public void cancel(Long id) {
        Order order = orderMapper.findById(id);
        if (order == null || !order.getUserId().equals(SecurityContext.currentUserId())) {
            throw new BizException(404, "order not found");
        }
        cancelInternal(order);
    }

    @Transactional
    public int cancelExpired() {
        List<Order> expired = orderMapper.findExpired(LocalDateTime.now(), 100);
        for (Order order : expired) {
            cancelInternal(order);
        }
        return expired.size();
    }

    private void cancelInternal(Order order) {
        if (!order.getStatus().equals(OrderStatus.PENDING_PAYMENT.code())) {
            throw new BizException("only pending orders can be cancelled");
        }
        if (orderMapper.cancelPending(order.getId()) == 0) {
            return;
        }
        List<OrderItem> items = orderItemMapper.findByOrderId(order.getId());
        for (OrderItem item : items) {
            gameMapper.increaseStock(item.getGameId(), item.getQuantity());
        }
    }

    private List<OrderLineRequest> resolveLines(CreateOrderRequest request) {
        if (Boolean.TRUE.equals(request.fromCart)) {
            List<CartView> cartItems = cartService.list();
            List<OrderLineRequest> lines = new ArrayList<>();
            for (CartView cartItem : cartItems) {
                OrderLineRequest line = new OrderLineRequest();
                line.gameId = cartItem.gameId;
                line.quantity = cartItem.quantity;
                lines.add(line);
            }
            return lines;
        }
        return request.items;
    }

    private String newOrderNo() {
        return "GM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
