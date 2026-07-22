package com.gamemall.game;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class GameListItem {
    public Long id;
    public Long categoryId;
    public String categoryName;
    public String title;
    public String developer;
    public BigDecimal price;
    public Integer stock;
    public Integer soldCount;
    public String coverUrl;
    public List<GameTag> tags = Collections.emptyList();
}
