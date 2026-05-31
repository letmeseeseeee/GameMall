package com.gamemall;

import com.gamemall.cart.CartService;
import com.gamemall.common.BizException;
import com.gamemall.game.Game;
import com.gamemall.game.GameListItem;
import com.gamemall.game.GameMapper;
import com.gamemall.order.*;
import com.gamemall.security.SecurityUser;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class OrderServiceConcurrencyTest {

    @Test
    void sameIdempotencyKeyReturnsOneOrderUnderConcurrency() throws Exception {
        FakeGameMapper gameMapper = new FakeGameMapper(10);
        FakeOrderMapper orderMapper = new FakeOrderMapper();
        FakeOrderItemMapper orderItemMapper = new FakeOrderItemMapper();
        OrderService orderService = new OrderService(orderMapper, orderItemMapper, gameMapper, Mockito.mock(CartService.class), 30);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch ready = new CountDownLatch(30);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<OrderDetail>> futures = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            futures.add(pool.submit(() -> {
                setUser(9000L);
                ready.countDown();
                start.await(3, TimeUnit.SECONDS);
                CreateOrderRequest request = new CreateOrderRequest();
                request.fromCart = false;
                request.idempotencyKey = "same-key";
                OrderLineRequest line = new OrderLineRequest();
                line.gameId = 1L;
                line.quantity = 1;
                request.items = Collections.singletonList(line);
                try {
                    return orderService.create(request);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }));
        }

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();
        List<Long> orderIds = new ArrayList<>();
        for (Future<OrderDetail> future : futures) {
            orderIds.add(future.get(5, TimeUnit.SECONDS).order.getId());
        }
        pool.shutdownNow();

        assertThat(orderIds.stream().distinct()).hasSize(1);
        assertThat(orderMapper.createdOrders()).isEqualTo(1);
        assertThat(orderItemMapper.items).hasSize(1);
        assertThat(gameMapper.stock.get()).isEqualTo(9);
    }

    @Test
    void concurrentCreateOrdersDoesNotOversellStock() throws Exception {
        FakeGameMapper gameMapper = new FakeGameMapper(40);
        FakeOrderMapper orderMapper = new FakeOrderMapper();
        FakeOrderItemMapper orderItemMapper = new FakeOrderItemMapper();
        OrderService orderService = new OrderService(orderMapper, orderItemMapper, gameMapper, Mockito.mock(CartService.class), 30);
        ExecutorService pool = Executors.newFixedThreadPool(32);
        CountDownLatch ready = new CountDownLatch(200);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            int index = i;
            futures.add(pool.submit(() -> {
                setUser(1000L + index);
                ready.countDown();
                start.await(3, TimeUnit.SECONDS);
                CreateOrderRequest request = new CreateOrderRequest();
                request.fromCart = false;
                request.idempotencyKey = "stress-" + index;
                OrderLineRequest line = new OrderLineRequest();
                line.gameId = 1L;
                line.quantity = 1;
                request.items = Collections.singletonList(line);
                try {
                    orderService.create(request);
                    success.incrementAndGet();
                } catch (BizException e) {
                    rejected.incrementAndGet();
                } finally {
                    SecurityContextHolder.clearContext();
                }
                return null;
            }));
        }

        ready.await(3, TimeUnit.SECONDS);
        start.countDown();
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        assertThat(success.get()).isEqualTo(40);
        assertThat(rejected.get()).isEqualTo(160);
        assertThat(gameMapper.stock.get()).isZero();
        assertThat(gameMapper.minStockObserved.get()).isZero();
        assertThat(orderItemMapper.items).hasSize(40);
        assertThat(orderMapper.createdOrders()).isEqualTo(40);
    }

    private static void setUser(Long userId) {
        SecurityUser user = new SecurityUser(userId, "user" + userId, "USER");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static class FakeGameMapper implements GameMapper {
        private final AtomicInteger stock;
        private final AtomicInteger minStockObserved;

        private FakeGameMapper(int stock) {
            this.stock = new AtomicInteger(stock);
            this.minStockObserved = new AtomicInteger(stock);
        }

        @Override
        public List<GameListItem> search(Long categoryId, String keyword, int offset, int limit) {
            return Collections.emptyList();
        }

        @Override
        public long count(Long categoryId, String keyword) {
            return 0;
        }

        @Override
        public Game findOnlineById(Long id) {
            Game game = new Game();
            game.setId(id);
            game.setTitle("Concurrent Game");
            game.setPrice(new BigDecimal("10.00"));
            game.setStock(stock.get());
            game.setStatus(1);
            return game;
        }

        @Override
        public Game findById(Long id) {
            return findOnlineById(id);
        }

        @Override
        public List<GameListItem> hot(int limit) {
            return Collections.emptyList();
        }

        @Override
        public int insert(Game game) {
            return 1;
        }

        @Override
        public int update(Game game) {
            return 1;
        }

        @Override
        public int updateStatus(Long id, int status) {
            return 1;
        }

        @Override
        public int decreaseStock(Long id, int quantity) {
            while (true) {
                int current = stock.get();
                if (current < quantity) {
                    return 0;
                }
                int next = current - quantity;
                if (stock.compareAndSet(current, next)) {
                    minStockObserved.updateAndGet(value -> Math.min(value, next));
                    return 1;
                }
            }
        }

        @Override
        public int increaseStock(Long id, int quantity) {
            stock.addAndGet(quantity);
            return 1;
        }

        @Override
        public int increaseSold(Long id, int quantity) {
            return 1;
        }
    }

    private static class FakeOrderMapper implements OrderMapper {
        private final AtomicLong ids = new AtomicLong();
        private final Map<Long, Order> orders = new ConcurrentHashMap<>();
        private final Map<String, Long> idempotency = new ConcurrentHashMap<>();

        @Override
        public int insert(Order order) {
            long id = ids.incrementAndGet();
            order.setId(id);
            Long existing = idempotency.putIfAbsent(order.getUserId() + ":" + order.getIdempotencyKey(), id);
            if (existing != null) {
                throw new DuplicateKeyException("duplicate idempotency key");
            }
            orders.put(id, cloneOrder(order));
            return 1;
        }

        @Override
        public int markCreated(Long id, BigDecimal totalAmount) {
            Order order = orders.get(id);
            if (order == null || !order.getStatus().equals(OrderStatus.CREATING.code())) {
                return 0;
            }
            order.setTotalAmount(totalAmount);
            order.setStatus(OrderStatus.PENDING_PAYMENT.code());
            return 1;
        }

        @Override
        public List<Order> listByUser(Long userId, int offset, int limit) {
            return Collections.emptyList();
        }

        @Override
        public Order findById(Long id) {
            return cloneOrder(orders.get(id));
        }

        @Override
        public Order findByOrderNo(String orderNo) {
            return null;
        }

        @Override
        public Order findByIdempotencyKey(Long userId, String idempotencyKey) {
            Long id = idempotency.get(userId + ":" + idempotencyKey);
            return id == null ? null : findById(id);
        }

        @Override
        public int markPaid(Long id) {
            return 1;
        }

        @Override
        public int cancelPending(Long id) {
            return 1;
        }

        @Override
        public List<Order> findExpired(LocalDateTime now, int limit) {
            return Collections.emptyList();
        }

        private int createdOrders() {
            return (int) orders.values().stream()
                    .filter(order -> order.getStatus().equals(OrderStatus.PENDING_PAYMENT.code()))
                    .count();
        }

        private Order cloneOrder(Order source) {
            if (source == null) {
                return null;
            }
            Order copy = new Order();
            copy.setId(source.getId());
            copy.setOrderNo(source.getOrderNo());
            copy.setIdempotencyKey(source.getIdempotencyKey());
            copy.setUserId(source.getUserId());
            copy.setTotalAmount(source.getTotalAmount());
            copy.setStatus(source.getStatus());
            copy.setPaymentStatus(source.getPaymentStatus());
            copy.setPaidAt(source.getPaidAt());
            copy.setExpireAt(source.getExpireAt());
            copy.setCreatedAt(source.getCreatedAt());
            copy.setUpdatedAt(source.getUpdatedAt());
            return copy;
        }
    }

    private static class FakeOrderItemMapper implements OrderItemMapper {
        private final List<OrderItem> items = new CopyOnWriteArrayList<>();

        @Override
        public int insert(OrderItem item) {
            items.add(item);
            return 1;
        }

        @Override
        public List<OrderItem> findByOrderId(Long orderId) {
            List<OrderItem> result = new ArrayList<>();
            for (OrderItem item : items) {
                if (item.getOrderId().equals(orderId)) {
                    result.add(item);
                }
            }
            return result;
        }
    }
}
