package com.cobblemon.mdks.cobblepass.command.subcommand;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.cobblemon.mdks.cobblepass.util.Utils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewCommand extends Subcommand {
    public ViewCommand() {
        super("§9Usage: §3/battlepass view");
    }

    @Override
    public CommandNode<CommandSourceStack> build() {
        return Commands.literal("view")
            .executes(this::run)
            .build();
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(
                Component.literal(Constants.ERROR_PREFIX + "This command must be run by a player!")
            );
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        showBattlePassInfo(player);
        return 1;
    }

    public static void showBattlePassInfo(ServerPlayer player) {
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(player);
        
        // Create info button showing level and XP
        int currentXP = pass.getXP();
        int xpForNext = (int)(CobblePass.config.getXpPerLevel() * Math.pow(Constants.XP_MULTIPLIER, pass.getLevel() - 1));
        GooeyButton infoButton = GooeyButton.builder()
            .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
            .with(DataComponents.CUSTOM_NAME, Component.literal("§bBattle Pass Progress"))
            .with(DataComponents.LORE, new ItemLore(Arrays.asList(
                Component.literal(String.format("§3Level: §f%d", pass.getLevel())),
                Component.literal(String.format("§3XP: §f%d§7/§f%d", currentXP, xpForNext))
            )))
            .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        // Create premium status button
        ItemStack premiumDisplay = pass.isPremium() ? new ItemStack(Items.GOLDEN_APPLE) : new ItemStack(Items.APPLE);
        List<Component> premiumLore = new ArrayList<>();
        if (pass.isPremium()) {
            if (pass.getPremiumExpiry() > 0) {
                long timeLeft = pass.getPremiumExpiry() - System.currentTimeMillis();
                premiumLore.add(Component.literal("§aActive"));
                premiumLore.add(Component.literal("§7Expires in: " + Utils.formatDuration(timeLeft)));
            } else {
                premiumLore.add(Component.literal("§aActive §7(Permanent)"));
            }
        } else {
            premiumLore.add(Component.literal("§cInactive"));
            premiumLore.add(Component.literal("§7Click to upgrade!"));
        }
        
        GooeyButton premiumButton = GooeyButton.builder()
            .display(premiumDisplay)
            .with(DataComponents.CUSTOM_NAME, Component.literal("§6Premium Status"))
            .with(DataComponents.LORE, new ItemLore(premiumLore))
            .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .onClick(action -> {
                if (!pass.isPremium()) {
                    player.sendSystemMessage(Component.literal("§7Use §f/battlepass premium §7to unlock premium rewards"));
                }
            })
            .build();

        // Create template for tier display with placeholders
        ChestTemplate baseTemplate = ChestTemplate.builder(6)
            .set(0, 4, infoButton)
            .set(0, 8, premiumButton)
            .fill(new PlaceholderButton()) // Fill with placeholders
            .build();

        // Create tier buttons
        List<Button> tierButtons = new ArrayList<>();
        for (BattlePassTier tier : CobblePass.battlePass.getTiers()) {
            List<Component> lore = new ArrayList<>();
            
            // Free reward
            if (tier.hasFreeReward(player.level().registryAccess())) {
                ItemStack freeReward = tier.getFreeRewardItem(player.level().registryAccess());
                String freeStatus = pass.hasClaimedFreeReward(tier.getLevel()) ? "§7[Claimed]" : "§a[Click to Claim]";
                lore.add(Component.literal("§fFree: " + freeReward.getDisplayName().getString() + " " + freeStatus));
            }

            // Premium reward
            if (tier.hasPremiumReward(player.level().registryAccess())) {
                ItemStack premiumReward = tier.getPremiumRewardItem(player.level().registryAccess());
                String premiumStatus;
                if (!pass.isPremium()) {
                    premiumStatus = "§c[Premium Only]";
                } else if (pass.hasClaimedPremiumReward(tier.getLevel())) {
                    premiumStatus = "§7[Claimed]";
                } else {
                    premiumStatus = "§6[Click to Claim]";
                }
                lore.add(Component.literal("§6Premium: " + premiumReward.getDisplayName().getString() + " " + premiumStatus));
            }

            ItemStack display = tier.getLevel() <= pass.getLevel() ? new ItemStack(Items.CHEST) : new ItemStack(Items.BARRIER);
            
            GooeyButton tierButton = GooeyButton.builder()
                .display(display)
                .with(DataComponents.CUSTOM_NAME, Component.literal("§3Level " + tier.getLevel()))
                .with(DataComponents.LORE, new ItemLore(lore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (tier.getLevel() <= pass.getLevel()) {
                        player.sendSystemMessage(Component.literal("§7Use §f/battlepass claim " + tier.getLevel() + " §7to claim rewards"));
                    } else {
                        player.sendSystemMessage(Component.literal("§cReach level " + tier.getLevel() + " to unlock these rewards!"));
                    }
                })
                .build();

            tierButtons.add(tierButton);
        }

        // Create paginated display
        LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(baseTemplate, tierButtons, null);
        firstPage.setTitle("§3Battle Pass");

        // Add navigation buttons to all pages
        LinkedPage current = firstPage;
        while (current != null) {
            if (current.getNext() != null) {
                LinkedPageButton nextBtn = LinkedPageButton.builder()
                    .display(new ItemStack(Items.ARROW))
                    .with(DataComponents.CUSTOM_NAME, Component.literal("§fNext Page"))
                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .linkType(LinkType.Next)
                    .build();
                ((ChestTemplate) current.getTemplate()).set(5, 8, nextBtn);
            }
            
            if (current.getPrevious() != null) {
                LinkedPageButton prevBtn = LinkedPageButton.builder()
                    .display(new ItemStack(Items.ARROW))
                    .with(DataComponents.CUSTOM_NAME, Component.literal("§fPrevious Page"))
                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .linkType(LinkType.Previous)
                    .build();
                ((ChestTemplate) current.getTemplate()).set(5, 0, prevBtn);
            }
            
            current = (LinkedPage) current.getNext();
        }

        // Open the UI
        UIManager.openUIForcefully(player, firstPage);
    }
}
