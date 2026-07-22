package com.gamemall.game;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {
    @Select("select c.id, c.name, c.code, c.description, c.sort_order, count(g.id) game_count " +
            "from categories c left join games g on g.category_id = c.id and g.status = 1 " +
            "group by c.id, c.name, c.code, c.description, c.sort_order order by c.sort_order asc, c.id asc")
    List<Category> findAll();
}
