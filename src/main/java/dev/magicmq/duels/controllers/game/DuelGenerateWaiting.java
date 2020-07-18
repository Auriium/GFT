package dev.magicmq.duels.controllers.game;

import dev.magicmq.duels.controllers.player.DuelsPlayer;

import java.util.HashSet;
import java.util.UUID;

public class DuelGenerateWaiting {

    private final UUID gameUniqueId;
    private final TemplateWorld templateWorld;
    private final HashSet<DuelsPlayer> players;
    private final DuelType type;

    public DuelGenerateWaiting(UUID gameUniqueId, TemplateWorld templateWorld, HashSet<DuelsPlayer> players, DuelType type) {
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

    public DuelType getType() {
        return type;
    }
}
