package dev.magicmq.duels.controllers.game;

import dev.magicmq.duels.controllers.player.DuelsPlayer;

import java.util.HashSet;

public class DuelWaiting {

    private final HashSet<DuelsPlayer> players;
    private final DuelType type;

    public DuelWaiting(HashSet<DuelsPlayer> players, DuelType type) {
        this.players = players;
        this.type = type;
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public DuelType getType() {
        return type;
    }
}
