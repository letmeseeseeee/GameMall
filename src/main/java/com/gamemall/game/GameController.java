package com.gamemall.game;

import com.gamemall.common.ApiResponse;
import com.gamemall.common.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Games")
@RestController
@RequestMapping("/api")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        return ApiResponse.ok(gameService.categories());
    }

    @GetMapping("/tags")
    public ApiResponse<List<GameTag>> tags() {
        return ApiResponse.ok(gameService.tags());
    }

    @GetMapping("/games")
    public ApiResponse<PageResult<GameListItem>> games(GameQuery query) {
        return ApiResponse.ok(gameService.search(query));
    }

    @GetMapping("/games/hot")
    public ApiResponse<List<GameListItem>> hot(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(gameService.hot(limit));
    }

    @GetMapping("/games/{id}")
    public ApiResponse<Game> detail(@PathVariable Long id) {
        return ApiResponse.ok(gameService.detail(id));
    }
}
