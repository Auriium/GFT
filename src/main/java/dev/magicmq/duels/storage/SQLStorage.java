package dev.magicmq.duels.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.magicmq.duels.Duels;
import dev.magicmq.duels.config.PluginConfig;
import dev.magicmq.duels.controllers.player.DuelsPlayer;
import dev.magicmq.duels.controllers.player.PlayerController;
import dev.magicmq.rappu.Database;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

public class SQLStorage {

    private static SQLStorage instance;

    private Database database;
    private Gson gson;

    private SQLStorage() {
        ConfigurationSection config = PluginConfig.getSqlInfo();
        database = new Database()
                .withPluginUsing(Duels.get())
                .withUsername(config.getString("username"))
                .withPassword(config.getString("password"))
                .withConnectionInfo(config.getString("host"), config.getInt("port"), config.getString("database"), false)
                .withDefaultProperties()
                .open();

        try {
            database.createTableFromFile("table.sql", Duels.class);
        } catch (SQLException | IOException e) {
            Duels.get().getLogger().log(Level.SEVERE, "Error when initializing the Duels SQL table! See this error:");
            e.printStackTrace();
        }

        gson = new Gson();
    }

    public void shutdown() {
        for (DuelsPlayer player : PlayerController.get().getPlayers()) {
            if (player.isInDatabase())
                updatePlayer(player, false);
            else
                insertPlayer(player, false);
        }
        database.close();
    }

    public void loadPlayer(Player player) {
        String sql = "SELECT * FROM `duels_player` ";
        sql += "WHERE `player_uuid` = ?;";
        database.queryAsync(sql, new Object[]{player.getUniqueId().toString()}, resultSet -> {
            if (resultSet.next()) {
                PlayerController.get().joinCallback(
                        player,
                        resultSet.getInt("kills"),
                        resultSet.getInt("deaths"),
                        resultSet.getInt("wins"),
                        resultSet.getInt("games_played"),
                        resultSet.getInt("losses"),
                        resultSet.getInt("shots_fired"),
                        resultSet.getInt("shots_hit"),
                        gson.fromJson(resultSet.getString("unlocked_kits"), new TypeToken<ArrayList<String>>() {}.getType()),
                        true);
            } else {
                PlayerController.get().joinCallback(player, 0, 0, 0, 0, 0, 0, 0, new ArrayList<>(), false);
            }
        });
    }

    public void insertPlayer(DuelsPlayer player, boolean async) {
        String sql = "INSERT INTO `duels_player` ";
        sql += "(`player_uuid`, `kills`, `deaths`, `wins`, `games_played`, `losses`, `shots_fired`, `shots_hit`, `unlocked_kits`) ";
        sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        Object[] toSet = new Object[]{
                player.getUniqueId().toString(),
                player.getKills(),
                player.getDeaths(),
                player.getWins(),
                player.getGamesPlayed(),
                player.getLosses(),
                player.getShotsFired(),
                player.getShotsHit(),
                gson.toJson(player.getUnlockedKits())
        };
        executeUpdate(player, sql, toSet, async);
    }

    public void updatePlayer(DuelsPlayer player, boolean async) {
        String sql = "UPDATE `duels_player` SET ";
        sql += "`kills` = ?, `deaths` = ?, `wins` = ?, `games_played` = ?, `losses` = ?, `shots_fired` = ?, `shots_hit` = ?, `unlocked_kits` = ? ";
        sql += "WHERE `player_uuid` = ?;";
        Object[] toSet = new Object[]{
                player.getKills(),
                player.getDeaths(),
                player.getWins(),
                player.getGamesPlayed(),
                player.getLosses(),
                player.getShotsFired(),
                player.getShotsHit(),
                gson.toJson(player.getUnlockedKits()),
                player.getUniqueId().toString()
        };
        executeUpdate(player, sql, toSet, async);
    }

    private void executeUpdate(DuelsPlayer player, String sql, Object[] toSet, boolean async) {
        if (async)
            database.updateAsync(sql, toSet, integer -> player.setInDatabase(true));
        else {
            try {
                database.update(sql, toSet);
                player.setInDatabase(true);
            } catch (SQLException e) {
                Duels.get().getLogger().log(Level.SEVERE, "There was an error when saving a player's data to the Duels SQL table! See this error:");
                e.printStackTrace();
            }
        }
    }

    public static SQLStorage get() {
        if (instance == null) {
            instance = new SQLStorage();
        }
        return instance;
    }
}
