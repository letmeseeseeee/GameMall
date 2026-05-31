package com.gamemall.order;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderItemMapper {
    @Insert("insert into order_items(order_id,game_id,game_title,price,quantity,subtotal) " +
            "values(#{orderId},#{gameId},#{gameTitle},#{price},#{quantity},#{subtotal})")
    int insert(OrderItem item);

    @Select("select * from order_items where order_id=#{orderId}")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);
}
