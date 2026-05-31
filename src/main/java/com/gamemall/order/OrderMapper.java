package com.gamemall.order;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderMapper {
    @Insert("insert into orders(order_no,idempotency_key,user_id,total_amount,status,payment_status,expire_at,created_at,updated_at) " +
            "values(#{orderNo},#{idempotencyKey},#{userId},#{totalAmount},#{status},#{paymentStatus},#{expireAt},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("update orders set total_amount=#{totalAmount},status=10,updated_at=now() where id=#{id} and status=5")
    int markCreated(@Param("id") Long id, @Param("totalAmount") java.math.BigDecimal totalAmount);

    @Select("select * from orders where user_id=#{userId} order by id desc limit #{limit} offset #{offset}")
    List<Order> listByUser(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from orders where id=#{id}")
    Order findById(@Param("id") Long id);

    @Select("select * from orders where order_no=#{orderNo}")
    Order findByOrderNo(@Param("orderNo") String orderNo);

    @Select("select * from orders where user_id=#{userId} and idempotency_key=#{idempotencyKey}")
    Order findByIdempotencyKey(@Param("userId") Long userId, @Param("idempotencyKey") String idempotencyKey);

    @Update("update orders set status=20,payment_status=1,paid_at=now(),updated_at=now() where id=#{id} and status=10")
    int markPaid(@Param("id") Long id);

    @Update("update orders set status=30,updated_at=now() where id=#{id} and status=10")
    int cancelPending(@Param("id") Long id);

    @Select("select * from orders where status=10 and expire_at < #{now} limit #{limit}")
    List<Order> findExpired(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
