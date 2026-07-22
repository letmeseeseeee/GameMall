package com.gamemall.admin;

import com.gamemall.common.ApiResponse;
import com.gamemall.game.Game;
import com.gamemall.game.GameRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Games")
@RestController
@RequestMapping("/api/admin/games")
public class AdminGameController {
    private final AdminGameService adminGameService;

    public AdminGameController(AdminGameService adminGameService) {
        this.adminGameService = adminGameService;
    }

    @PostMapping
    public ApiResponse<Game> create(@Validated @RequestBody GameRequest request) {
        return ApiResponse.ok(adminGameService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Validated @RequestBody GameRequest request) {
        adminGameService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable Long id, @RequestParam int status) {
        adminGameService.updateStatus(id, status);
        return ApiResponse.ok(null);
    }
}
