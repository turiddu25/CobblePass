package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.util.Logger;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;

import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

public class DefeatPokemonListener {
    private static final Logger LOGGER = new Logger("CobblePass");

    public static void register() {
        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, DefeatPokemonListener::handle);
    }

    private static Unit handle(BattleFaintedEvent event) {
        // Get the fainted Pokémon
        BattleActor faintedActor = event.getKilled().getActor();
        
        // Iterate through all actors to find opponents
        for (BattleActor actor : event.getBattle().getActors()) {
            // Skip if it's the fainted actor or not a player
            if (actor == faintedActor || !(actor instanceof PlayerBattleActor)) {
                continue;
            }
            
            PlayerBattleActor playerActor = (PlayerBattleActor) actor;
            ServerPlayer player = playerActor.getEntity();
            
            if (player == null) {
                LOGGER.debug("PlayerBattleActor does not have an associated ServerPlayer. Skipping.");
                continue;
            }

            // Get the player's Battle Pass
            PlayerBattlePass battlePass = CobblePass.battlePass.getPlayerPass(player);
            if (battlePass == null) {
                LOGGER.debug("No Battle Pass found for player: " + player.getName().getString());
                continue;
            }

            // Award XP based on configuration
            int xpToAward = CobblePass.config.getDefeatXP();
            battlePass.addXP(xpToAward);

            LOGGER.debug("Awarded " + xpToAward + " XP to " + player.getName().getString() + " for defeating a Pokémon.");
        }

        return Unit.INSTANCE;
    }
}
