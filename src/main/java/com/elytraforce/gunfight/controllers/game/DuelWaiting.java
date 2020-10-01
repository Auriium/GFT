package com.elytraforce.gunfight.controllers.game;

import java.util.HashSet;
              

import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

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
