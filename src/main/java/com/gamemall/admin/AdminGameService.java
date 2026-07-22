package com.gamemall.admin;

import com.gamemall.game.Game;
import com.gamemall.game.GameMapper;
import com.gamemall.game.GameRequest;
import com.gamemall.game.GameService;
import com.gamemall.game.GameTagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminGameService {
    private final GameMapper gameMapper;
    private final GameTagMapper gameTagMapper;
    private final GameService gameService;

    public AdminGameService(GameMapper gameMapper, GameTagMapper gameTagMapper, GameService gameService) {
        this.gameMapper = gameMapper;
        this.gameTagMapper = gameTagMapper;
        this.gameService = gameService;
    }

    @Transactional
    public Game create(GameRequest request) {
        Game game = toGame(new Game(), request);
        gameMapper.insert(game);
        replaceTags(game.getId(), request.tagIds);
        game.setTags(gameTagMapper.findByGameIds(Collections.singletonList(game.getId())));
        gameService.evict(game.getId());
        return game;
    }

    @Transactional
    public void update(Long id, GameRequest request) {
        Game game = toGame(new Game(), request);
        game.setId(id);
        gameMapper.update(game);
        replaceTags(id, request.tagIds);
        gameService.evict(id);
    }

    public void updateStatus(Long id, int status) {
        gameMapper.updateStatus(id, status);
        gameService.evict(id);
    }

    private void replaceTags(Long gameId, List<Long> tagIds) {
        gameTagMapper.deleteByGameId(gameId);
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> normalizedTagIds = tagIds.stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(8)
                    .collect(Collectors.toList());
            if (!normalizedTagIds.isEmpty()) {
                gameTagMapper.insertLinks(gameId, normalizedTagIds);
            }
        }
    }

    private Game toGame(Game game, GameRequest request) {
        game.setCategoryId(request.categoryId);
        game.setTitle(request.title);
        game.setDeveloper(request.developer);
        game.setPublisher(request.publisher);
        game.setPrice(request.price);
        game.setStock(request.stock);
        game.setCoverUrl(request.coverUrl);
        game.setDescription(request.description);
        game.setReleaseDate(request.releaseDate);
        return game;
    }
}
