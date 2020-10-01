package com.elytraforce.gunfight.controllers.game;

public enum DuelType {

    ONE_V_ONE(2, "1v1"),
    TWO_V_TWO(4, "2v2"),
    THREE_V_THREE(6, "3v3");

    private int maxPlayers;
    private String displayName;

    DuelType(int maxPlayers, String displayName) {
        this.maxPlayers = maxPlayers;
        this.displayName = displayName;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
