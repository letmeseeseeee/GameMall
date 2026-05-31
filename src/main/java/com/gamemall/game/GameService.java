package com.gamemall.game;

import com.gamemall.common.BizException;
import com.gamemall.common.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class GameService {
    private final GameMapper gameMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration detailTtl;
    private final Duration hotTtl;

    public GameService(GameMapper gameMapper, CategoryMapper categoryMapper, RedisTemplate<String, Object> redisTemplate,
                       @Value("${gamemall.cache.game-detail-ttl-minutes}") long detailTtlMinutes,
                       @Value("${gamemall.cache.hot-games-ttl-minutes}") long hotTtlMinutes) {
        this.gameMapper = gameMapper;
        this.categoryMapper = categoryMapper;
        this.redisTemplate = redisTemplate;
        this.detailTtl = Duration.ofMinutes(detailTtlMinutes);
        this.hotTtl = Duration.ofMinutes(hotTtlMinutes);
    }

    public List<Category> categories() {
        return categoryMapper.findAll();
    }

    public PageResult<GameListItem> search(GameQuery query) {
        int page = query.page == null || query.page < 1 ? 1 : query.page;
        int size = query.size == null || query.size < 1 ? 10 : Math.min(query.size, 50);
        long total = gameMapper.count(query.categoryId, query.keyword);
        List<GameListItem> records = gameMapper.search(query.categoryId, query.keyword, (page - 1) * size, size);
        return new PageResult<>(records, total, page, size);
    }

    @SuppressWarnings("unchecked")
    public List<GameListItem> hot(int limit) {
        String key = "gamemall:hot:" + limit;
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                return (List<GameListItem>) cached;
            }
        } catch (Exception ignored) {
        }
        List<GameListItem> games = gameMapper.hot(Math.min(Math.max(limit, 1), 20));
        try {
            redisTemplate.opsForValue().set(key, games, hotTtl);
        } catch (Exception ignored) {
        }
        return games;
    }

    public Game detail(Long id) {
        String key = "gamemall:game:" + id;
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
        try {
            redisTemplate.opsForValue().set(key, game, detailTtl);
        } catch (Exception ignored) {
        }
        return game;
    }

    public void evict(Long gameId) {
        try {
            redisTemplate.delete("gamemall:game:" + gameId);
            redisTemplate.delete("gamemall:hot:10");
        } catch (Exception ignored) {
        }
    }
}
