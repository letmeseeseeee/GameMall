package com.gamemall.game;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class GameRequest {
    @NotNull
    public Long categoryId;
    @NotBlank
    public String title;
    public String developer;
    public String publisher;
    @NotNull
    @DecimalMin("0.00")
    public BigDecimal price;
    @NotNull
    @Min(0)
    public Integer stock;
    public String coverUrl;
    public String description;
    public LocalDateTime releaseDate;
}
