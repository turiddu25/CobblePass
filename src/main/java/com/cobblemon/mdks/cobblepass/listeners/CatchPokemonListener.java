package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.config.Config;
import com.cobblemon.mdks.cobblepass.util.Logger;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import kotlin.Unit;
import net.minecraft.server.level.ServerPlayer;

public class CatchPokemonListener {
    private static final Logger LOGGER = new Logger("CobblePass");

    public static void register() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, CatchPokemonListener::handle);
    }

    private static Unit handle(PokemonCapturedEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            PlayerBattlePass battlePass = CobblePass.battlePass.getPlayerPass(player);
            if (battlePass != null) {
                int xp = CobblePass.config.getCatchXP();
                battlePass.addXP(xp);
                LOGGER.debug("Awarded " + xp + " XP to " + player.getName().getString() + " for catching a Pokémon");
            }
        }
        return Unit.INSTANCE;
    }
}
