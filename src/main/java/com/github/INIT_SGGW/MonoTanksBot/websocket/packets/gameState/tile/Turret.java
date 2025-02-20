package com.github.INIT_SGGW.MonoTanksBot.websocket.packets.gameState.tile;

import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.github.INIT_SGGW.MonoTanksBot.utils.OptionalDeserializer;

/**
 * Represents a turret of a tank.
 */
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public record Turret(
        /**
         * The direction the turret is facing.
         */
        Direction direction,

        /**
         * The number of bullets available in the turret.
         * You only see your own bullet count.
         */
        @JsonDeserialize(using = OptionalDeserializer.class) Optional<Long> bulletCount,

        /**
         * The number of ticks remaining for the turret to regenerate a bullet.
         * You only see your own turret regeneration, if any.
         */
        @JsonDeserialize(using = OptionalDeserializer.class) Optional<Double> ticksToRegenBullet) {
}