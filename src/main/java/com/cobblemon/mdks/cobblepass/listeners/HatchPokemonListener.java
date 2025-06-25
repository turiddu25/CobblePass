package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mdks.cobblepass.CobblePass;
import net.minecraft.server.level.ServerPlayer;
import kotlin.Unit;

public class HatchPokemonListener {
    public static void register() {
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.NORMAL, evt -> {
            ServerPlayer player = evt.getPlayer();
            if (player != null) {
                CobblePass.battlePass.addXP(player, CobblePass.config.getHatchXP());
            }
            return Unit.INSTANCE;
        });
    }
}