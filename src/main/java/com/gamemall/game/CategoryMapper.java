package com.gamemall.game;

import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CategoryMapper {
    @Select("select * from categories order by sort_order asc, id asc")
    List<Category> findAll();
}
