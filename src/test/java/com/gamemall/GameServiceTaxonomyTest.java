package com.gamemall;

import com.gamemall.common.PageResult;
import com.gamemall.game.CategoryMapper;
import com.gamemall.game.GameListItem;
import com.gamemall.game.GameMapper;
import com.gamemall.game.GameQuery;
import com.gamemall.game.GameService;
import com.gamemall.game.GameTag;
import com.gamemall.game.GameTagMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameServiceTaxonomyTest {

    @Test
    void combinesCategoryAndTagFiltersAndHydratesGameTags() {
        GameMapper gameMapper = mock(GameMapper.class);
        CategoryMapper categoryMapper = mock(CategoryMapper.class);
        GameTagMapper gameTagMapper = mock(GameTagMapper.class);
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        GameService service = new GameService(gameMapper, categoryMapper, gameTagMapper, redisTemplate, 30, 10);

        GameQuery query = new GameQuery();
        query.categoryId = 1L;
        query.tagId = 12L;
        query.keyword = "Neon";
        query.page = 1;
        query.size = 20;

        GameListItem game = new GameListItem();
        game.id = 7L;
        game.title = "Neon Ronin";
        GameTag tag = new GameTag();
        tag.gameId = 7L;
        tag.id = 12L;
        tag.name = "赛博朋克";
        tag.groupName = "题材";

        when(gameMapper.count(1L, 12L, "Neon")).thenReturn(1L);
        when(gameMapper.search(1L, 12L, "Neon", 0, 20)).thenReturn(Collections.singletonList(game));
        when(gameTagMapper.findByGameIds(Collections.singletonList(7L))).thenReturn(Collections.singletonList(tag));

        PageResult<GameListItem> result = service.search(query);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).tags).extracting(value -> value.name).containsExactly("赛博朋克");
        verify(gameMapper).count(1L, 12L, "Neon");
        verify(gameMapper).search(1L, 12L, "Neon", 0, 20);
    }
}
