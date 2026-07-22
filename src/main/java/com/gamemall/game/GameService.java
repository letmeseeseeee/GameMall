package com.gamemall.game;

import com.gamemall.common.BizException;
import com.gamemall.common.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameMapper gameMapper;
    private final CategoryMapper categoryMapper;
    private final GameTagMapper gameTagMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration detailTtl;
    private final Duration hotTtl;

    public GameService(GameMapper gameMapper, CategoryMapper categoryMapper, GameTagMapper gameTagMapper,
                       RedisTemplate<String, Object> redisTemplate,
                       @Value("${gamemall.cache.game-detail-ttl-minutes}") long detailTtlMinutes,
                       @Value("${gamemall.cache.hot-games-ttl-minutes}") long hotTtlMinutes) {
        this.gameMapper = gameMapper;
        this.categoryMapper = categoryMapper;
        this.gameTagMapper = gameTagMapper;
        this.redisTemplate = redisTemplate;
        this.detailTtl = Duration.ofMinutes(detailTtlMinutes);
        this.hotTtl = Duration.ofMinutes(hotTtlMinutes);
    }

    public List<Category> categories() {
        return categoryMapper.findAll();
    }

    public List<GameTag> tags() {
        return gameTagMapper.findAll();
    }

    public PageResult<GameListItem> search(GameQuery query) {
        int page = query.page == null || query.page < 1 ? 1 : query.page;
        int size = query.size == null || query.size < 1 ? 10 : Math.min(query.size, 50);
        long total = gameMapper.count(query.categoryId, query.tagId, query.keyword);
        List<GameListItem> records = gameMapper.search(query.categoryId, query.tagId, query.keyword,
                (page - 1) * size, size);
        attachTags(records);
        return new PageResult<>(records, total, page, size);
    }

    @SuppressWarnings("unchecked")
    public List<GameListItem> hot(int limit) {
        String key = "gamemall:v2:hot:" + limit;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                return (List<GameListItem>) cached;
            }
        } catch (Exception ignored) {
        }
        List<GameListItem> games = gameMapper.hot(Math.min(Math.max(limit, 1), 20));
        attachTags(games);
        try {
            redisTemplate.opsForValue().set(key, games, hotTtl);
        } catch (Exception ignored) {
        }
        return games;
    }

    public Game detail(Long id) {
        String key = "gamemall:v2:game:" + id;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Game) {
                return (Game) cached;
            }
        } catch (Exception ignored) {
        }
        Game game = gameMapper.findOnlineById(id);
        if (game == null) {
            throw new BizException(404, "game not found");
        }
        game.setTags(tagsForGame(id));
        try {
            redisTemplate.opsForValue().set(key, game, detailTtl);
        } catch (Exception ignored) {
        }
        return game;
    }

    public void evict(Long gameId) {
        try {
            redisTemplate.delete("gamemall:v2:game:" + gameId);
            List<String> hotKeys = new ArrayList<>();
            for (int limit = 1; limit <= 20; limit++) {
                hotKeys.add("gamemall:v2:hot:" + limit);
            }
            redisTemplate.delete(hotKeys);
        } catch (Exception ignored) {
        }
    }

    private void attachTags(List<GameListItem> games) {
        if (games == null || games.isEmpty()) {
            return;
        }
        List<Long> gameIds = games.stream().map(game -> game.id).collect(Collectors.toList());
        Map<Long, List<GameTag>> tagsByGame = new HashMap<>();
        for (GameTag tag : gameTagMapper.findByGameIds(gameIds)) {
            tagsByGame.computeIfAbsent(tag.gameId, ignored -> new ArrayList<>()).add(tag);
        }
        for (GameListItem game : games) {
            game.tags = tagsByGame.getOrDefault(game.id, Collections.emptyList());
        }
    }

    private List<GameTag> tagsForGame(Long gameId) {
        List<GameTag> tags = gameTagMapper.findByGameIds(Collections.singletonList(gameId));
        return tags == null ? Collections.emptyList() : tags;
    }
}
