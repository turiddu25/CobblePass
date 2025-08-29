package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.storage.ReleasePokemonEvent;
import net.minecraft.server.level.ServerPlayer;

import kotlin.Unit;

public class ReleasePokemonListener {
    public static void register() {
        CobblemonEvents.POKEMON_RELEASED_EVENT_POST.subscribe(Priority.NORMAL, (ReleasePokemonEvent.Post event) -> {
            ServerPlayer player = event.getPlayer();
            if (player != null) {
                CobblePass.battlePass.addXP(player, CobblePass.config.getReleaseXP());
            }
            return Unit.INSTANCE;
        });
    }
}