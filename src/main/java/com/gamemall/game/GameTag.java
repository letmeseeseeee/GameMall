package com.gamemall.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GameTag {
    @JsonIgnore
    public Long gameId;
    public Long id;
    public String name;
    public String groupName;
}
