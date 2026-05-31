package com.gamemall.admin;

import com.gamemall.common.ApiResponse;
import com.gamemall.game.Game;
import com.gamemall.game.GameMapper;
import com.gamemall.game.GameRequest;
import com.gamemall.game.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Games")
@RestController
@RequestMapping("/api/admin/games")
public class AdminGameController {
    private final GameMapper gameMapper;
    private final GameService gameService;

    public AdminGameController(GameMapper gameMapper, GameService gameService) {
        this.gameMapper = gameMapper;
        this.gameService = gameService;
    }

    @PostMapping
    public ApiResponse<Game> create(@Validated @RequestBody GameRequest request) {
        Game game = toGame(new Game(), request);
        gameMapper.insert(game);
        gameService.evict(game.getId());
        return ApiResponse.ok(game);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody GameRequest request) {
        Game game = toGame(new Game(), request);
        game.setId(id);
        gameMapper.update(game);
        gameService.evict(id);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @RequestParam int status) {
        gameMapper.updateStatus(id, status);
        gameService.evict(id);
        return ApiResponse.ok(null);
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
