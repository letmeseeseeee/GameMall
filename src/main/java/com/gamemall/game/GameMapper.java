package com.gamemall.game;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface GameMapper {
    @Select("<script>" +
            "select g.*, c.name category_name from games g left join categories c on g.category_id = c.id " +
            "where g.status = 1 " +
            "<if test='categoryId != null'>and g.category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'>and g.title like concat('%', #{keyword}, '%') </if>" +
            "order by g.sold_count desc, g.id desc limit #{limit} offset #{offset}" +
            "</script>")
    List<GameListItem> search(@Param("categoryId") Long categoryId, @Param("keyword") String keyword,
                              @Param("offset") int offset, @Param("limit") int limit);

    @Select("<script>" +
            "select count(*) from games g where g.status = 1 " +
            "<if test='categoryId != null'>and g.category_id = #{categoryId} </if>" +
            "<if test='keyword != null and keyword != \"\"'>and g.title like concat('%', #{keyword}, '%') </if>" +
            "</script>")
    long count(@Param("categoryId") Long categoryId, @Param("keyword") String keyword);

    @Select("select * from games where id = #{id} and status = 1")
    Game findOnlineById(@Param("id") Long id);

    @Select("select * from games where id = #{id}")
    Game findById(@Param("id") Long id);

    @Select("select g.*, c.name category_name from games g left join categories c on g.category_id = c.id " +
            "where g.status = 1 order by g.sold_count desc, g.id desc limit #{limit}")
    List<GameListItem> hot(@Param("limit") int limit);

    @Insert("insert into games(category_id,title,developer,publisher,price,stock,sold_count,cover_url,description,status,release_date,created_at,updated_at) " +
            "values(#{categoryId},#{title},#{developer},#{publisher},#{price},#{stock},0,#{coverUrl},#{description},1,#{releaseDate},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Game game);

    @Update("update games set category_id=#{categoryId},title=#{title},developer=#{developer},publisher=#{publisher},price=#{price},stock=#{stock}," +
            "cover_url=#{coverUrl},description=#{description},release_date=#{releaseDate},updated_at=now() where id=#{id}")
    int update(Game game);

    @Update("update games set status=#{status},updated_at=now() where id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    @Update("update games set stock = stock - #{quantity}, updated_at=now() where id = #{id} and status = 1 and stock >= #{quantity}")
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("update games set stock = stock + #{quantity}, updated_at=now() where id = #{id}")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("update games set sold_count = sold_count + #{quantity}, updated_at=now() where id = #{id}")
    int increaseSold(@Param("id") Long id, @Param("quantity") int quantity);
}
