package com.gamemall.cart;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface CartMapper {
    @Select("select * from cart_items where user_id=#{userId} and game_id=#{gameId}")
    CartItem find(@Param("userId") Long userId, @Param("gameId") Long gameId);

    @Insert("insert into cart_items(user_id,game_id,quantity,created_at,updated_at) values(#{userId},#{gameId},#{quantity},now(),now())")
    int insert(CartItem item);

    @Update("update cart_items set quantity=#{quantity},updated_at=now() where user_id=#{userId} and game_id=#{gameId}")
    int updateQuantity(@Param("userId") Long userId, @Param("gameId") Long gameId, @Param("quantity") int quantity);

    @Select("select ci.id, ci.game_id, g.title, g.cover_url, g.price, g.stock, ci.quantity, g.price * ci.quantity subtotal " +
            "from cart_items ci join games g on ci.game_id = g.id where ci.user_id=#{userId} order by ci.updated_at desc")
    List<CartView> list(@Param("userId") Long userId);

    @Delete("delete from cart_items where user_id=#{userId} and game_id=#{gameId}")
    int delete(@Param("userId") Long userId, @Param("gameId") Long gameId);

    @Delete("delete from cart_items where user_id=#{userId}")
    int clear(@Param("userId") Long userId);
}
