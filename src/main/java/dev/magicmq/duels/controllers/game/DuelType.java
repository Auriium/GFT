package dev.magicmq.duels.controllers.game;

public enum DuelType {

    ONE_V_ONE(2),
    TWO_V_TWO(4),
    THREE_V_THREE(6);

    private int maxPlayers;

    DuelType(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }
}
