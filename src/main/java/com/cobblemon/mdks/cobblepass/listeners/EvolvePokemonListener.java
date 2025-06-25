package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionCompleteEvent;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mdks.cobblepass.CobblePass;
import net.minecraft.server.level.ServerPlayer;
import kotlin.Unit;

import java.util.UUID;

public class EvolvePokemonListener {
    public static void register() {
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe(Priority.NORMAL, evt -> {
            Pokemon pokemon = evt.getPokemon();
            UUID ownerUUID = pokemon.getOwnerUUID();
            if (ownerUUID != null) {
                ServerPlayer player = CobblePass.server.getPlayerList().getPlayer(ownerUUID);
                if (player != null) {
                    CobblePass.battlePass.addXP(player, CobblePass.config.getEvolveXP());
                }
            }
            return Unit.INSTANCE;
        });
    }
}