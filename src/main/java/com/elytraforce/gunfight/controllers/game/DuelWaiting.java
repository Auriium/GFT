package com.elytraforce.gunfight.controllers.game;

import java.util.HashSet;

import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

public class DuelWaiting {

    private final HashSet<DuelsPlayer> players;
    private final GameType.Type type;

    public DuelWaiting(HashSet<DuelsPlayer> players, GameType.Type type) {
        this.players = players;
        this.type = type;
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public GameType.Type getType() {
        return type;
    }
}
