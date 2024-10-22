package com.github.INIT_SGGW.MonoTanksClient.Agent;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.INIT_SGGW.MonoTanksClient.AgentAbstraction.AbilityType;
import com.github.INIT_SGGW.MonoTanksClient.AgentAbstraction.Agent;
import com.github.INIT_SGGW.MonoTanksClient.AgentAbstraction.AgentResponse;
import com.github.INIT_SGGW.MonoTanksClient.AgentAbstraction.MoveDirection;
import com.github.INIT_SGGW.MonoTanksClient.AgentAbstraction.RotationDirection;
import com.github.INIT_SGGW.MonoTanksClient.websocket.Warning;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameEnd.GameEnd;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameEnd.GameEndPlayer;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.GameState;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.ItemType;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Direction;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Bullet;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Item;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Laser;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.LaserDirection;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Mine;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Tank;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.TileEntity;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.gameState.tile.Tile.Wall;
import com.github.INIT_SGGW.MonoTanksClient.websocket.packets.lobbyData.LobbyData;

public class MyAgent extends Agent {
    private static final Logger logger = LoggerFactory.getLogger(MyAgent.class);

    private final String myId;

    /**
     * Constructor for the Agent class.
     *
     * @param lobbyData Initial lobby data used to create the agent.
     */
    public MyAgent(LobbyData lobbyData) {
        super(lobbyData);
        this.myId = lobbyData.playerId();
    }

    /**
     * Method to handle subsequent lobby data received after the initial data.
     *
     * @param lobbyData The subsequent lobby data.
     */
    @Override
    public void onSubsequentLobbyData(LobbyData lobbyData) {

    }

    /**
     * Method to determine the next move of the agent based on the current game
     * state.
     *
     * @param gameState The current state of the game.
     * @return An AgentResponse representing the agent's next move.
     */
    @Override
    public AgentResponse nextMove(GameState gameState) {

        // Print map as ascii
        System.out.println("Map:");
        for (Tile[] row : gameState.map()) {
            for (Tile tile : row) {
                List<TileEntity> entities = tile.getEntities();
                String symbol = " ";

                if (tile.isVisible()) {
                    symbol = ".";
                }

                for (TileEntity entity : entities) {
                    if (entity instanceof Tank tank) {
                        if (tank.getOwnerId().equals(myId)) {
                            if (tank.getDirection() == Direction.UP) {
                                symbol = "^";
                            } else if (tank.getDirection() == Direction.DOWN) {
                                symbol = "v";
                            } else if (tank.getDirection() == Direction.LEFT) {
                                symbol = "<";
                            } else if (tank.getDirection() == Direction.RIGHT) {
                                symbol = ">";
                            }

                            // There is also turret direction
                            // tank.getTurret().direction()

                        } else {
                            symbol = "T";
                        }
                    } else if (entity instanceof Wall) {
                        symbol = "#";
                    } else if (entity instanceof Bullet bullet) {
                        if (bullet.getDirection() == Direction.UP) {
                            symbol = "↑";
                        } else if (bullet.getDirection() == Direction.DOWN) {
                            symbol = "↓";
                        } else if (bullet.getDirection() == Direction.LEFT) {
                            symbol = "←";
                        } else if (bullet.getDirection() == Direction.RIGHT) {
                            symbol = "→";
                        }
                    } else if (entity instanceof Laser laser) {
                        if (laser.getOrientation() == LaserDirection.VERTICAL) {
                            symbol = "|";
                        } else if (laser.getOrientation() == LaserDirection.HORIZONTAL) {
                            symbol = "-";
                        }
                    } else if (entity instanceof Mine) {
                        symbol = "X";
                    } else if (entity instanceof Item item) {
                        if (item.getItemType() == ItemType.DOUBLE_BULLET) {
                            symbol = "D";
                        } else if (item.getItemType() == ItemType.LASER) {
                            symbol = "L";
                        } else if (item.getItemType() == ItemType.MINE) {
                            symbol = "M";
                        } else if (item.getItemType() == ItemType.RADAR) {
                            symbol = "R";
                        }
                    }
                }

                if (tile.getZoneIndex().isPresent()) {
                    int zoneIndex = tile.getZoneIndex().get();
                    if (tile.isVisible()) {
                        symbol = String.valueOf((char) zoneIndex);
                    } else {
                        symbol = String.valueOf((char) (zoneIndex + 32));
                    }
                }

                System.out.print(symbol + " ");
            }
            System.out.println();
        }

        // Find my tank
        Tank myTank = null;
        for (Tile[] row : gameState.map()) {
            for (Tile tile : row) {
                for (TileEntity object : tile.getEntities()) {
                    if (object instanceof Tank tank && tank.getOwnerId().equals(myId)) {
                        myTank = tank;
                    }
                }
            }
        }

        // If we cannot find our tank, we are dead.
        if (myTank == null) {
            return AgentResponse.createPassResponse();
        }

        double random = Math.random();

        if (random < 0.2) {
            if (Math.random() < 0.5) {
                return AgentResponse.createMoveResponse(MoveDirection.FORWARD);
            } else {
                return AgentResponse.createMoveResponse(MoveDirection.BACKWARD);
            }
        } else if (random < 0.4) {
            double tankRandom = Math.random();
            RotationDirection tankRotation = tankRandom < 0.33 ? RotationDirection.LEFT
                    : tankRandom < 0.66 ? RotationDirection.RIGHT : null;

            double turretRandom = Math.random();
            RotationDirection turretRotation = turretRandom < 0.33 ? RotationDirection.LEFT
                    : turretRandom < 0.66 ? RotationDirection.RIGHT : null;

            return AgentResponse.createRotationResponse(Optional.ofNullable(tankRotation),
                    Optional.ofNullable(turretRotation));
        } else if (random < 0.6) {
            return AgentResponse.createAbilityUseResponse(AbilityType.FIRE_BULLET);
        } else if (random < 0.7) {
            return AgentResponse.createAbilityUseResponse(AbilityType.FIRE_DOUBLE_BULLET);
        } else if (random < 0.8) {
            return AgentResponse.createAbilityUseResponse(AbilityType.USE_LASER);
        } else if (random < 0.9) {
            return AgentResponse.createAbilityUseResponse(AbilityType.USE_RADAR);
        } else if (random < 0.95) {
            return AgentResponse.createAbilityUseResponse(AbilityType.DROP_MINE);
        } else {
            return AgentResponse.createPassResponse();
        }
    }

    /**
     * Called when a warning is received from the server.
     * Please, do remember that if your agent is stuck on processing warning,
     * the next move won't be called and vice versa.
     *
     * @param warning The warning received from the server.
     */
    @Override
    public void onWarningReceived(Warning warning, Optional<String> message) {
        switch (warning) {
            case PLAYER_ALREADY_MADE_ACTION_WARNING -> {
                logger.warn("⚠️ Player already made action warning");
            }
            case MISSING_GAME_STATE_ID_WARNING -> {
                logger.warn("⚠️ Missing game state ID warning");
            }
            case SLOW_RESPONSE_WARNING -> {
                logger.warn("⚠️ Slow response warning");
            }
            case ACTION_IGNORED_DUE_TO_DEAD_WARNING -> {
                logger.warn("⚠️ Action ignored due to dead warning");
            }
            case CUSTOM_WARNING -> {
                String msg = message.orElse("No message");
                logger.warn("⚠️ Custom Warning: {}", msg);
            }
        }
    }

    /**
     * Method to handle the end of the game.
     *
     * @param gameEnd The final state of the game when it ends.
     */
    @Override
    public void onGameEnd(GameEnd gameEnd) {
        logger.info("🏁 Game ended");
        GameEndPlayer winner = gameEnd.players()[0];
        if (winner.id().equals(this.myId)) {
            logger.info("🏆 I won!");
        }

        for (GameEndPlayer player : gameEnd.players()) {
            logger.info("👤 {} - {}", player.nickname(), player.score());
        }
    }
}
