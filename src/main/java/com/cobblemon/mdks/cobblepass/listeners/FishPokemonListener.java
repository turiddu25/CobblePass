package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;

import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

public class FishPokemonListener {
    public static void register() {
        CobblemonEvents.BOBBER_SPAWN_POKEMON_POST.subscribe(Priority.NORMAL, event -> {
            if (event.getBobber().getPlayerOwner() instanceof ServerPlayer player) {
                CobblePass.battlePass.addXP(player, CobblePass.config.getFishXP());
            }
            return Unit.INSTANCE;
        });
    }
}