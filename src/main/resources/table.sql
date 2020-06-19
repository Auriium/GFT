CREATE TABLE IF NOT EXISTS `duels_player` (
    `player_id`     INT(4)       NOT NULL AUTO_INCREMENT,
    `player_uuid`   CHAR(36)     NOT NULL UNIQUE,
    `kills`         INT(4)       NOT NULL,
    `deaths`        INT(4)       NOT NULL,
    `wins`          INT(4)       NOT NULL,
    `games_played`  INT(4)       NOT NULL,
    `losses`        INT(4)       NOT NULL,
    `shots_fired`   INT(4)       NOT NULL,
    `unlocked_kits` VARCHAR(100) NOT NULL,
PRIMARY KEY (`player_id`),
KEY (`player_uuid`)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = latin1;
