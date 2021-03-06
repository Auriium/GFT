package com.elytraforce.gunfight.controllers.game;

import java.util.HashSet;
import java.util.UUID;

import com.elytraforce.gunfight.controllers.game.gamemodes.GameType;
import com.elytraforce.gunfight.controllers.player.DuelsPlayer;

public class DuelGenerateWaiting {

    private final UUID gameUniqueId;
    private final TemplateWorld templateWorld;
    private final HashSet<DuelsPlayer> players;
    private final GameType.Type type;

    public DuelGenerateWaiting(UUID gameUniqueId, TemplateWorld templateWorld, HashSet<DuelsPlayer> players, GameType.Type type) {
        this.gameUniqueId = gameUniqueId;
        this.templateWorld = templateWorld;
        this.players = players;
        this.type = type;
    }

    public UUID getGameUniqueId() {
        return gameUniqueId;
    }

    public boolean doesUniqueIdEqual(String uuid) {
        return gameUniqueId.toString().equals(uuid);
    }

    public TemplateWorld getTemplateWorld() {
        return templateWorld;
    }

    public HashSet<DuelsPlayer> getPlayers() {
        return players;
    }

    public GameType.Type getType() {
        return type;
    }
}
