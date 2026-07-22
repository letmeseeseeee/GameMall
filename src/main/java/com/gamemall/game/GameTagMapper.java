package com.gamemall.game;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

public interface GameTagMapper {
    @Select("select id, name, group_name from tags order by sort_order asc, id asc")
    List<GameTag> findAll();

    @Select("<script>" +
            "select gt.game_id, t.id, t.name, t.group_name from game_tags gt " +
            "join tags t on t.id = gt.tag_id where gt.game_id in " +
            "<foreach collection='gameIds' item='gameId' open='(' separator=',' close=')'>#{gameId}</foreach> " +
            "order by t.sort_order asc, t.id asc" +
            "</script>")
    List<GameTag> findByGameIds(@Param("gameIds") Collection<Long> gameIds);

    @Delete("delete from game_tags where game_id = #{gameId}")
    int deleteByGameId(@Param("gameId") Long gameId);

    @Insert("<script>insert into game_tags(game_id, tag_id) values " +
            "<foreach collection='tagIds' item='tagId' separator=','>(#{gameId}, #{tagId})</foreach>" +
            "</script>")
    int insertLinks(@Param("gameId") Long gameId, @Param("tagIds") Collection<Long> tagIds);
}
