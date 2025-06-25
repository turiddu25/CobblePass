package com.cobblemon.mdks.cobblepass.listeners;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.TradeCompletedEvent;
import com.cobblemon.mod.common.trade.PlayerTradeParticipant;
import com.cobblemon.mdks.cobblepass.CobblePass;
import net.minecraft.server.level.ServerPlayer;
import kotlin.Unit;

public class TradePokemonListener {
    public static void register() {
        CobblemonEvents.TRADE_COMPLETED.subscribe(Priority.NORMAL, evt -> {
            if (evt.getTradeParticipant1() instanceof PlayerTradeParticipant) {
                ServerPlayer player1 = ((PlayerTradeParticipant) evt.getTradeParticipant1()).getPlayer();
                CobblePass.battlePass.addXP(player1, CobblePass.config.getTradeXP());
            }
            if (evt.getTradeParticipant2() instanceof PlayerTradeParticipant) {
                ServerPlayer player2 = ((PlayerTradeParticipant) evt.getTradeParticipant2()).getPlayer();
                CobblePass.battlePass.addXP(player2, CobblePass.config.getTradeXP());
            }
            return Unit.INSTANCE;
        });
    }
}