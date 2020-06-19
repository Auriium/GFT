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
                .withConnectionInfo(config.getString("host"), config.getInt("port"), config.getString("database"))
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

    public void shutdown() throws SQLException {
        for (DuelsPlayer player : PlayerController.get().getPlayers()) {
            String sql = "INSERT INTO `duels_player` ";
            sql += "(`player_uuid`, `kills`, `deaths`, `wins`, `games_played`, `losses`, `shots_fired`, `unlocked_kits`) ";
            sql += "VALUES (?, ?, ?, ?, ?, ?, ?) ";
            sql += "ON DUPLICATE KEY UPDATE ";
            sql += "`kills` = ?, `deaths` = ?, `wins` = ?, `games_played` = ?, `losses` = ?, `shots_fired` = ?, `unlocked_kits` = ?;";
            Object[] toSet = new Object[]{
                    player.asBukkitPlayer().getUniqueId().toString(),
                    player.getKills(),
                    player.getDeaths(),
                    player.getWins(),
                    player.getGamesPlayed(),
                    player.getLosses(),
                    player.getShotsFired(),
                    gson.toJson(player.getUnlockedKits()),
                    player.getKills(),
                    player.getDeaths(),
                    player.getWins(),
                    player.getGamesPlayed(),
                    player.getLosses(),
                    player.getShotsFired(),
                    gson.toJson(player.getUnlockedKits())
            };
            database.update(sql, toSet);
        }
        database.close();
    }

    public void loadPlayer(Player player) {
        String sql = "SELECT * FROM `duels_player` ";
        sql += "WHERE `player_uuid` = ?;";
        database.queryAsync(sql, new Object[]{player.getUniqueId().toString()}, resultSet -> {
            try {
                if (resultSet.next()) {
                    PlayerController.get().joinCallback(
                            player,
                            resultSet.getInt("kills"),
                            resultSet.getInt("deaths"),
                            resultSet.getInt("wins"),
                            resultSet.getInt("games_played"),
                            resultSet.getInt("losses"),
                            resultSet.getInt("shots_fired"),
                            gson.fromJson(resultSet.getString("unlocked_kits"), new TypeToken<ArrayList<String>>() {}.getType()));
                } else {
                    PlayerController.get().joinCallback(player, 0, 0, 0, 0, 0, 0, new ArrayList<>());
                }
            } catch (SQLException e) {
                Duels.get().getLogger().log(Level.SEVERE, "Error when loading a player's data from the Duels SQL table! See this error:");
                e.printStackTrace();
            } finally {
                try {
                    resultSet.close();
                } catch (SQLException ignored) {}
            }
        });
    }

    public void savePlayer(DuelsPlayer player) {
        String sql = "INSERT INTO `duels_player` ";
        sql += "(`player_uuid`, `kills`, `deaths`, `wins`, `games_played`, `losses`, `shots_fired`) ";
        sql += "VALUES (?, ?, ?, ?, ?, ?, ?) ";
        sql += "ON DUPLICATE KEY UPDATE ";
        sql += "`kills` = ?, `deaths` = ?, `wins` = ?, `games_played` = ?, `losses` = ?, `shots_fired` = ?;";
        Object[] toSet = new Object[]{
                player.asBukkitPlayer().getUniqueId().toString(),
                player.getKills(),
                player.getDeaths(),
                player.getWins(),
                player.getGamesPlayed(),
                player.getLosses(),
                player.getShotsFired(),
                player.getKills(),
                player.getDeaths(),
                player.getWins(),
                player.getGamesPlayed(),
                player.getLosses(),
                player.getShotsFired()
        };
        database.updateAsync(sql, toSet, integer -> {});
    }

    public static SQLStorage get() {
        if (instance == null) {
            instance = new SQLStorage();
        }
        return instance;
    }
}
