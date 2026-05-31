package com.gamemall.user;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {
    @Select("select * from users where username = #{username} limit 1")
    User findByUsername(@Param("username") String username);

    @Select("select * from users where id = #{id}")
    User findById(@Param("id") Long id);

    @Insert("insert into users(username,password_hash,nickname,role,status,created_at,updated_at) " +
            "values(#{username},#{passwordHash},#{nickname},#{role},#{status},now(),now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
}
