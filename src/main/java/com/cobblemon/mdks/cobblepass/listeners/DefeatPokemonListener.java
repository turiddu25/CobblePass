package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.config.Config;
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
        // Get the player who caused the faint
        if (event.getKilled() != null) {
            BattleActor actor = event.getKilled().getActor();
            if (actor instanceof PlayerBattleActor playerActor) {
                ServerPlayer player = playerActor.getEntity();
                PlayerBattlePass battlePass = CobblePass.battlePass.getPlayerPass(player);
                if (battlePass != null) {
                    int xp = CobblePass.config.getDefeatXP();
                    battlePass.addXP(xp);
                    LOGGER.debug("Awarded " + xp + " XP to " + player.getName().getString() + " for defeating a Pokémon");
                }
            }
        }
        return Unit.INSTANCE;
    }
}
