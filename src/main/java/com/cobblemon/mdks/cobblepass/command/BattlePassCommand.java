package com.cobblemon.mdks.cobblepass.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.battlepass.BattlePassTier;
import com.cobblemon.mdks.cobblepass.battlepass.PlayerBattlePass;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddLevelsCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.AddXPCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ClaimCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.CreateCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.DeleteCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ManagePremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.PremiumCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.ReloadCommand;
import com.cobblemon.mdks.cobblepass.command.subcommand.SeasonCommand;
import com.cobblemon.mdks.cobblepass.config.GuiIngredient;
import com.cobblemon.mdks.cobblepass.config.GuiStructure;
import com.cobblemon.mdks.cobblepass.data.Reward;
import com.cobblemon.mdks.cobblepass.util.BaseCommand;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import com.cobblemon.mdks.cobblepass.util.Permissions;
import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.google.gson.JsonObject;
import com.mojang.brigadier.context.CommandContext;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public class BattlePassCommand extends BaseCommand {
    public BattlePassCommand() {
        super("battlepass",
                Constants.COMMAND_ALIASES,
                new Permissions().getPermission(Constants.PERM_COMMAND_BASE),
                List.of(
                        new ClaimCommand(),
                        new AddLevelsCommand(),
                        new PremiumCommand(),
                        new ReloadCommand(),
                        new CreateCommand(),
                        new AddXPCommand(),
                        new ManagePremiumCommand(),
                        new SeasonCommand(),
                        new DeleteCommand()
                )
        );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendSystemMessage(
                    LangManager.getComponent("lang.command.must_be_player")
            );
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (!CobblePass.config.isSeasonActive()) {
            if (context.getSource().hasPermission(4)) {
                showBattlePassInfo(player);
            } else {
                context.getSource().sendSystemMessage(
                        LangManager.getComponent("lang.season.no_active")
                );
            }
            return 1;
        }

        showBattlePassInfo(player);
        return 1;
    }

    private static String formatTimeRemaining(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;

        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }

    private static List<Component> getRewardLore(PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium) {
        List<Component> lore = new ArrayList<>();
        Reward reward = isPremium ? tier.getPremiumReward() : tier.getFreeReward();

        // Part 1: Add Reward Details (same as before)
        if (reward != null) {
            JsonObject data = reward.getData();
            switch (reward.getType()) {
                case ITEM:
                    if (data != null) {
                        String itemId = data.get("id").getAsString();
                        int count = data.has("Count") ? data.get("Count").getAsInt() : 1;
                        String[] parts = itemId.split(":");
                        String itemName = parts[parts.length - 1].replace("_", " ");
                        itemName = Arrays.stream(itemName.split(" "))
                                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                .collect(Collectors.joining(" "));
                        if (!parts[0].equals("minecraft")) {
                            String modName = parts[0].substring(0, 1).toUpperCase() + parts[0].substring(1);
                            lore.add(LangManager.getComponent("lang.reward.mod_item", modName));
                        }
                        lore.add(LangManager.getComponent("lang.reward.count", count, itemName));
                    } else {
                        lore.add(LangManager.getComponent("lang.reward.item"));
                    }
                    break;
                case POKEMON:
                    lore.add(LangManager.getComponent("lang.reward.pokemon"));
                    if (data != null) {
                        if (data.has("species")) {
                            String speciesName = data.get("species").getAsString();
                            speciesName = speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1);
                            lore.add(Component.literal(speciesName));
                        }
                        if (data.has("level")) {
                            lore.add(LangManager.getComponent("lang.reward.level", data.get("level").getAsInt()));
                        }
                        if (data.has("shiny") && data.get("shiny").getAsBoolean()) {
                            lore.add(LangManager.getComponent("lang.reward.shiny"));
                        }
                    }
                    break;
                case COMMAND:
                    if (data != null) {
                        if (data.has("display_name")) {
                            lore.add(Component.literal("§7" + data.get("display_name").getAsString()));
                        } else if (data.has("id")) {
                            String itemId = data.get("id").getAsString();
                            String[] parts = itemId.split(":");
                            String itemName = parts[parts.length - 1].replace("_", " ");
                            itemName = Arrays.stream(itemName.split(" "))
                                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                                    .collect(Collectors.joining(" "));
                            lore.add(Component.literal("§7" + itemName));
                        }
                    }
                    break;
            }
        }

        lore.add(Component.literal("")); // Spacer

        // Part 2: Add Status Information
        if (isPremium) {
            if (!pass.isPremium()) {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.requires_premium"));
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.purchase_prompt"));
            } else if (level > pass.getLevel()) {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.not_reached", level));
            } else if (pass.hasClaimedPremiumReward(level)) {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.claimed"));
            } else {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.available"));
            }
        } else { // Free Reward
            if (level > pass.getLevel()) {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.not_reached", level));
            } else if (pass.hasClaimedFreeReward(level)) {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.claimed"));
            } else {
                lore.add(LangManager.getComponent("lang.gui.status.in_lore.available"));
            }
        }
        return lore;
    }

    private static Button createRewardButton(ServerPlayer player, PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium, int pageNum) {
        Reward reward = isPremium ? tier.getPremiumReward() : tier.getFreeReward();
        ItemStack displayItem = isPremium ? tier.getPremiumRewardItem(player.level().registryAccess()) : tier.getFreeRewardItem(player.level().registryAccess());

        if (displayItem == null || displayItem.isEmpty()) {
            displayItem = new ItemStack(Items.BARRIER);
        }

        // The lore is now generated with status information included
        List<Component> lore = getRewardLore(pass, tier, level, isPremium);

        return GooeyButton.builder()
                .display(displayItem)
                .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.tier_title", level)) // Use a lang key for the title
                .with(DataComponents.LORE, new ItemLore(lore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (level > pass.getLevel()) {
                        player.sendSystemMessage(LangManager.getComponent("lang.command.level_not_reached", level));
                        return;
                    }

                    if (isPremium && !pass.isPremium()) {
                        player.sendSystemMessage(LangManager.getComponent("lang.command.not_premium"));
                        return;
                    }

                    if (CobblePass.battlePass.claimReward(player, level, isPremium)) {
                        player.sendSystemMessage(LangManager.getComponent("lang.command.reward_claim", level));
                        showBattlePassInfo(player, pageNum); // Refresh UI
                    }
                })
                .build();
    }


    private static Button createXpInfoButton(ServerPlayer player) {
        List<Component> xpInfoLore = new ArrayList<>();
        xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.description"));
        xpInfoLore.add(Component.literal(""));
        
        if (CobblePass.config.getCatchXP() > 0) {
            xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.catch", CobblePass.config.getCatchXP()));
        }
        if (CobblePass.config.getDefeatXP() > 0) {
            xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.defeat", CobblePass.config.getDefeatXP()));
        }
        if (CobblePass.config.getEvolveXP() > 0) {
            xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.evolve", CobblePass.config.getEvolveXP()));
        }
        if (CobblePass.config.getHatchXP() > 0) {
            xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.hatch", CobblePass.config.getHatchXP()));
        }
        if (CobblePass.config.getTradeXP() > 0) {
            xpInfoLore.add(LangManager.getComponent("lang.gui.xp_info.trade", CobblePass.config.getTradeXP()));
        }

        return GooeyButton.builder()
                .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
                .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.xp_info.name"))
                .with(DataComponents.LORE, new ItemLore(xpInfoLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();
    }

    private static Button createProgressButton(PlayerBattlePass pass) {
        int currentXP = pass.getXP();
        int xpForNext;
        if (CobblePass.config.getXpProgression().getMode().equalsIgnoreCase("MANUAL")) {
            xpForNext = CobblePass.config.getXpProgression().getManualXpForLevel(pass.getLevel() + 1);
        } else {
            xpForNext = (int)(CobblePass.config.getXpProgression().getXpPerLevel() * Math.pow(CobblePass.config.getXpProgression().getXpMultiplier(), pass.getLevel() - 1));
        }
        
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(LangManager.getComponent("lang.gui.progress.level", pass.getLevel()));
        infoLore.add(LangManager.getComponent("lang.gui.progress.xp", currentXP, xpForNext));

        if (CobblePass.config.isSeasonActive()) {
            long timeLeft = CobblePass.config.getSeasonEndTime() - System.currentTimeMillis();
            if (timeLeft > 0) {
                infoLore.add(Component.literal(""));
                infoLore.add(LangManager.getComponent("lang.gui.progress.time_remaining", formatTimeRemaining(timeLeft)));
            }
        }

        return GooeyButton.builder()
                .display(new ItemStack(Items.NETHER_STAR))
                .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.progress.name"))
                .with(DataComponents.LORE, new ItemLore(infoLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();
    }

    private static Button createPremiumStatusButton(PlayerBattlePass pass, ServerPlayer player) {
        ItemStack premiumDisplay = pass.isPremium() ? new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()) : new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item());
        List<Component> premiumLore = new ArrayList<>();
        
        if (pass.isPremium()) {
            premiumLore.add(LangManager.getComponent("lang.gui.premium.active"));
            if (CobblePass.config.isSeasonActive()) {
                premiumLore.add(LangManager.getComponent("lang.gui.premium.season", CobblePass.config.getCurrentSeason()));
            } else {
                premiumLore.add(LangManager.getComponent("lang.gui.premium.no_season"));
            }
        } else {
            premiumLore.add(LangManager.getComponent("lang.gui.premium.inactive"));
            premiumLore.add(LangManager.getComponent("lang.gui.premium.click_info"));
        }

        return GooeyButton.builder()
                .display(premiumDisplay)
                .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.premium.name"))
                .with(DataComponents.LORE, new ItemLore(premiumLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (!pass.isPremium()) {
                        player.closeContainer();
                        player.sendSystemMessage(LangManager.getComponent("lang.gui.premium.command_info"));
                    }
                })
                .build();
    }

    private static Button createStaticButton(GuiIngredient ingredient) {
        ItemStack stack = ingredient.createItemStack();
        
        // Process name and lore for localization
        String name = ingredient.getName();
        if (name != null && name.startsWith("lang.")) {
            name = LangManager.get(name);
        }
        
        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : ingredient.getLore()) {
            if (loreLine.startsWith("lang.")) {
                loreComponents.add(LangManager.getComponent(loreLine));
            } else {
                loreComponents.add(Component.literal(loreLine));
            }
        }

        GooeyButton.Builder builder = GooeyButton.builder().display(stack);
        
        if (name != null && !name.isEmpty()) {
            builder.with(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        
        if (!loreComponents.isEmpty()) {
            builder.with(DataComponents.LORE, new ItemLore(loreComponents));
        }
        
        if (ingredient.isHideTooltip()) {
            builder.with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        }

        return builder.build();
    }

    public static void showBattlePassInfo(ServerPlayer player) {
        showBattlePassInfo(player, 0);
    }

    public static void showBattlePassInfo(ServerPlayer player, int pageToShow) {
        PlayerBattlePass pass = CobblePass.battlePass.getPlayerPass(player);
        GuiStructure guiStructure = CobblePass.config.getGuiConfig().getStructure();

        Map<Integer, BattlePassTier> tiers = CobblePass.battlePass.getTiers();
        int totalTiers = tiers.values().stream().mapToInt(BattlePassTier::getLevel).max().orElse(0);

        // Calculate how many reward slots we have per page
        List<GuiStructure.SlotInfo> freeRewardSlots = guiStructure.findPlaceholderSlots(GuiIngredient.IngredientType.FREE_REWARD_PLACEHOLDER);
        List<GuiStructure.SlotInfo> premiumRewardSlots = guiStructure.findPlaceholderSlots(GuiIngredient.IngredientType.PREMIUM_REWARD_PLACEHOLDER);
        
        int rewardsPerPage = Math.max(freeRewardSlots.size(), premiumRewardSlots.size());
        if (rewardsPerPage == 0) rewardsPerPage = 7; // Fallback
        
        int totalPages = (int) Math.ceil((double) totalTiers / rewardsPerPage);

        List<LinkedPage> pages = new ArrayList<>();

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            ChestTemplate.Builder templateBuilder = ChestTemplate.builder(6);

            // Fill all slots based on structure
            for (int row = 0; row < GuiStructure.getGuiRows(); row++) {
                for (int col = 0; col < GuiStructure.getGuiCols(); col++) {
                    GuiIngredient ingredient = guiStructure.getIngredientAt(row, col);
                    if (ingredient == null) continue;

                    Button button = null;
                    switch (ingredient.getType()) {
                        case STATIC_ITEM:
                            button = createStaticButton(ingredient);
                            break;
                        case XP_INFO_PLACEHOLDER:
                            button = createXpInfoButton(player);
                            break;
                        case PROGRESS_PLACEHOLDER:
                            button = createProgressButton(pass);
                            break;
                        case PREMIUM_STATUS_PLACEHOLDER:
                            button = createPremiumStatusButton(pass, player);
                            break;
                        case FREE_REWARDS_LABEL:
                            button = GooeyButton.builder()
                                    .display(new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item()))
                                    .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.reward.free_label"))
                                    .build();
                            break;
                        case PREMIUM_REWARDS_LABEL:
                            button = GooeyButton.builder()
                                    .display(new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()))
                                    .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.reward.premium_label"))
                                    .build();
                            break;
                    }

                    if (button != null) {
                        templateBuilder.set(row, col, button);
                    }
                }
            }

            // Now handle reward placeholders
            int startLevel = (pageNum * rewardsPerPage) + 1;
            int endLevel = Math.min(startLevel + rewardsPerPage, totalTiers + 1);

            int rewardIndex = 0;
            for (int level = startLevel; level < endLevel; level++) {
                BattlePassTier tier = tiers.get(level);
                if (tier == null) continue;

                // Place free reward if slot available
                if (rewardIndex < freeRewardSlots.size() && tier.hasFreeReward()) {
                    GuiStructure.SlotInfo slot = freeRewardSlots.get(rewardIndex);
                    templateBuilder.set(slot.getRow(), slot.getCol(), createRewardButton(player, pass, tier, level, false, pageNum));
                }

                // Place premium reward if slot available
                if (rewardIndex < premiumRewardSlots.size() && tier.hasPremiumReward()) {
                    GuiStructure.SlotInfo slot = premiumRewardSlots.get(rewardIndex);
                    templateBuilder.set(slot.getRow(), slot.getCol(), createRewardButton(player, pass, tier, level, true, pageNum));
                }


                rewardIndex++;
            }

            ChestTemplate template = templateBuilder.build();
            
            LinkedPage page = LinkedPage.builder()
                    .template(template)
                    .title(LangManager.get("lang.gui.title"))
                    .build();

            pages.add(page);
        }

        // Link pages and add navigation
        for (int i = 0; i < pages.size(); i++) {
            LinkedPage current = pages.get(i);
            if (i > 0) {
                current.setPrevious(pages.get(i - 1));
            }
            if (i < pages.size() - 1) {
                current.setNext(pages.get(i + 1));
            }

            // Add navigation buttons
            ChestTemplate template = (ChestTemplate) current.getTemplate();
            List<GuiStructure.SlotInfo> prevSlots = guiStructure.findPlaceholderSlots(GuiIngredient.IngredientType.NAVIGATION_PREVIOUS);
            List<GuiStructure.SlotInfo> nextSlots = guiStructure.findPlaceholderSlots(GuiIngredient.IngredientType.NAVIGATION_NEXT);

            if (current.getPrevious() != null && !prevSlots.isEmpty()) {
                GuiStructure.SlotInfo slot = prevSlots.get(0);
                Button prevBtn = LinkedPageButton.builder()
                        .display(new ItemStack(Items.ARROW))
                        .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.navigation.previous"))
                        .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                        .linkType(LinkType.Previous)
                        .build();
                template.set(slot.getRow(), slot.getCol(), prevBtn);
            }
            
            if (current.getNext() != null && !nextSlots.isEmpty()) {
                GuiStructure.SlotInfo slot = nextSlots.get(0);
                Button nextBtn = LinkedPageButton.builder()
                        .display(new ItemStack(Items.ARROW))
                        .with(DataComponents.CUSTOM_NAME, LangManager.getComponent("lang.gui.navigation.next"))
                        .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                        .linkType(LinkType.Next)
                        .build();
                template.set(slot.getRow(), slot.getCol(), nextBtn);
            }
        }

        if (!pages.isEmpty()) {
            int targetPage = Math.max(0, Math.min(pageToShow, pages.size() - 1));
            UIManager.openUIForcefully(player, pages.get(targetPage));
        }
    }
}
