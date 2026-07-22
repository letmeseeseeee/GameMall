package com.gamemall.game;

public class Category {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer sortOrder;
    private Long gameCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getGameCount() {
        return gameCount;
    }

    public void setGameCount(Long gameCount) {
        this.gameCount = gameCount;
    }
}
