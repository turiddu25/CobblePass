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
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
                    LangManager.get("lang.command.must_be_player")
            );
            return 1;
        }

        ServerPlayer player = context.getSource().getPlayer();
        if (!CobblePass.config.isSeasonActive()) {
            if (context.getSource().hasPermission(4)) {
                showBattlePassInfo(player);
            } else {
                context.getSource().sendSystemMessage(
                        LangManager.get("lang.season.no_active")
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
                        
                        // Format: "Item Name x Count" without showing mod name or separate count line
                        lore.add(LangManager.get("lang.reward.item_format", itemName, count));
                    } else {
                        lore.add(LangManager.get("lang.reward.item"));
                    }
                    break;
                case POKEMON:
                    lore.add(LangManager.get("lang.reward.pokemon"));
                    if (data != null) {
                        if (data.has("species")) {
                            String speciesName = data.get("species").getAsString();
                            speciesName = speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1);
                            lore.add(Component.literal(speciesName));
                        }
                        if (data.has("level")) {
                            lore.add(LangManager.get("lang.reward.level", data.get("level").getAsInt()));
                        }
                        if (data.has("shiny") && data.get("shiny").getAsBoolean()) {
                            lore.add(LangManager.get("lang.reward.shiny"));
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
                lore.add(LangManager.get("lang.gui.status.in_lore.requires_premium"));
                lore.add(LangManager.get("lang.gui.status.in_lore.purchase_prompt"));
            } else if (level > pass.getLevel()) {
                lore.add(LangManager.get("lang.gui.status.in_lore.not_reached", level));
            } else if (pass.hasClaimedPremiumReward(level)) {
                lore.add(LangManager.get("lang.gui.status.in_lore.claimed"));
            } else {
                lore.add(LangManager.get("lang.gui.status.in_lore.available"));
            }
        } else { // Free Reward
            if (level > pass.getLevel()) {
                lore.add(LangManager.get("lang.gui.status.in_lore.not_reached", level));
            } else if (pass.hasClaimedFreeReward(level)) {
                lore.add(LangManager.get("lang.gui.status.in_lore.claimed"));
            } else {
                lore.add(LangManager.get("lang.gui.status.in_lore.available"));
            }
        }
        return lore;
    }

    private static Button createRewardButton(ServerPlayer player, PlayerBattlePass pass, BattlePassTier tier, int level, boolean isPremium, int pageNum) {
        // Use the consolidated display logic from BattlePassTier
        ItemStack displayItem = isPremium ?
            tier.getPremiumRewardItem(pass, player.level().registryAccess()) :
            tier.getFreeRewardItem(pass, player.level().registryAccess());

        if (displayItem == null || displayItem.isEmpty()) {
            displayItem = new ItemStack(Items.BARRIER);
        }

        // The lore is now generated with status information included
        List<Component> lore = getRewardLore(pass, tier, level, isPremium);

        return GooeyButton.builder()
                .display(displayItem)
                .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.tier_title", level)) // Use a lang key for the title
                .with(DataComponents.LORE, new ItemLore(lore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (level > pass.getLevel()) {
                        player.sendSystemMessage(LangManager.get("lang.command.level_not_reached", level));
                        return;
                    }

                    if (isPremium && !pass.isPremium()) {
                        player.sendSystemMessage(LangManager.get("lang.command.not_premium"));
                        return;
                    }

                    if (CobblePass.battlePass.claimReward(player, level, isPremium)) {
                        player.sendSystemMessage(LangManager.get("lang.command.reward_claim", level));
                        showBattlePassInfo(player, pageNum); // Refresh UI
                    }
                })
                .build();
    }


    private static Button createXpInfoButton(ServerPlayer player) {
        List<Component> xpInfoLore = new ArrayList<>();
        xpInfoLore.add(LangManager.get("lang.gui.xp_info.description"));
        xpInfoLore.add(Component.literal(""));
        
        // This is the correct way to get the XP values from the config
        Map<String, Integer> xpSources = new java.util.HashMap<>();
        xpSources.put("catch", CobblePass.config.getCatchXP());
        xpSources.put("defeat", CobblePass.config.getDefeatXP());
        xpSources.put("evolve", CobblePass.config.getEvolveXP());
        xpSources.put("hatch", CobblePass.config.getHatchXP());
        xpSources.put("trade", CobblePass.config.getTradeXP());
        xpSources.put("fish", CobblePass.config.getFishXP());
        xpSources.put("catch_legendary", CobblePass.config.getCatchLegendaryXP());
        xpSources.put("catch_shiny", CobblePass.config.getCatchShinyXP());
        xpSources.put("catch_ultrabeast", CobblePass.config.getCatchUltraBeastXP());
        xpSources.put("catch_mythical", CobblePass.config.getCatchMythicalXP());
        xpSources.put("catch_paradox", CobblePass.config.getCatchParadoxXP());
        xpSources.put("release", CobblePass.config.getReleaseXP());

        for (Map.Entry<String, Integer> entry : xpSources.entrySet()) {
            if (entry.getValue() > 0) {
                xpInfoLore.add(LangManager.get("lang.gui.info.lore." + entry.getKey(), entry.getValue()));
            }
        }

        return GooeyButton.builder()
                .display(new ItemStack(Items.EXPERIENCE_BOTTLE))
                .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.xp_info.name"))
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
        infoLore.add(LangManager.get("lang.gui.progress.level", pass.getLevel()));
        infoLore.add(LangManager.get("lang.gui.progress.xp", currentXP, xpForNext));

        if (CobblePass.config.isSeasonActive()) {
            long timeLeft = CobblePass.config.getSeasonEndTime() - System.currentTimeMillis();
            if (timeLeft > 0) {
                infoLore.add(Component.literal(""));
                infoLore.add(LangManager.get("lang.gui.progress.time_remaining", formatTimeRemaining(timeLeft)));
            }
        }

        return GooeyButton.builder()
                .display(new ItemStack(Items.NETHER_STAR))
                .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.progress.name"))
                .with(DataComponents.LORE, new ItemLore(infoLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();
    }

    private static Button createPremiumStatusButton(PlayerBattlePass pass, ServerPlayer player) {
        ItemStack premiumDisplay = pass.isPremium() ? new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()) : new ItemStack(PokeBalls.INSTANCE.getPREMIER_BALL().item());
        List<Component> premiumLore = new ArrayList<>();
        
        if (pass.isPremium()) {
            premiumLore.add(LangManager.get("lang.gui.premium.active"));
            if (CobblePass.config.isSeasonActive()) {
                premiumLore.add(LangManager.get("lang.gui.premium.season", CobblePass.config.getCurrentSeason()));
            } else {
                premiumLore.add(LangManager.get("lang.gui.premium.no_season"));
            }
        } else {
            premiumLore.add(LangManager.get("lang.gui.premium.inactive"));
            premiumLore.add(LangManager.get("lang.gui.premium.click_info"));
        }

        return GooeyButton.builder()
                .display(premiumDisplay)
                .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.premium.name"))
                .with(DataComponents.LORE, new ItemLore(premiumLore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    if (!pass.isPremium()) {
                        player.closeContainer();
                        player.sendSystemMessage(LangManager.get("lang.gui.premium.command_info"));
                    }
                })
                .build();
    }

    private static Button createStaticButton(GuiIngredient ingredient) {
        ItemStack stack = ingredient.createItemStack();
        
        // Process name and lore for localization
        // Process name and lore for localization
        Component name = null;
        if (ingredient.getName() != null && !ingredient.getName().isEmpty()) {
            name = ingredient.getName().startsWith("lang.")
                    ? LangManager.get(ingredient.getName())
                    : LangManager.asComponent(ingredient.getName());
        }

        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : ingredient.getLore()) {
            loreComponents.add(loreLine.startsWith("lang.")
                    ? LangManager.get(loreLine)
                    : LangManager.asComponent(loreLine));
        }

        GooeyButton.Builder builder = GooeyButton.builder().display(stack);

        if (name != null) {
            builder.with(DataComponents.CUSTOM_NAME, name);
        }
        
        if (!loreComponents.isEmpty()) {
            builder.with(DataComponents.LORE, new ItemLore(loreComponents));
        }
        
        // Always hide tooltips for all GUI elements
        builder.with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

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
                        case CUSTOM_ITEM:
                            button = createCustomItemButton(ingredient, player, pageNum);
                            break;
                        case COMMAND_BUTTON:
                            button = createCommandButton(ingredient, player, pageNum);
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
                                    .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.reward.free_label"))
                                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                                    .build();
                            break;
                        case PREMIUM_REWARDS_LABEL:
                            button = GooeyButton.builder()
                                    .display(new ItemStack(PokeBalls.INSTANCE.getMASTER_BALL().item()))
                                    .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.reward.premium_label"))
                                    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
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
                    .title(LangManager.getLegacy("lang.gui.title"))
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
                GuiIngredient prevIngredient = guiStructure.getIngredients().get('<');
                ItemStack prevDisplay = prevIngredient != null ? prevIngredient.createItemStack() : new ItemStack(Items.ARROW);
                Button prevBtn = LinkedPageButton.builder()
                        .display(prevDisplay)
                        .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.navigation.previous"))
                        .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                        .linkType(LinkType.Previous)
                        .build();
                template.set(slot.getRow(), slot.getCol(), prevBtn);
            }
            
            if (current.getNext() != null && !nextSlots.isEmpty()) {
                GuiStructure.SlotInfo slot = nextSlots.get(0);
                GuiIngredient nextIngredient = guiStructure.getIngredients().get('>');
                ItemStack nextDisplay = nextIngredient != null ? nextIngredient.createItemStack() : new ItemStack(Items.ARROW);
                Button nextBtn = LinkedPageButton.builder()
                        .display(nextDisplay)
                        .with(DataComponents.CUSTOM_NAME, LangManager.get("lang.gui.navigation.next"))
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

    /**
     * Creates a custom item button with optional command functionality
     */
    private static Button createCustomItemButton(GuiIngredient ingredient, ServerPlayer player, int pageNum) {
        ItemStack stack = ingredient.createItemStack();
        
        // Process name and lore for localization and color codes
        Component name = null;
        if (ingredient.getName() != null && !ingredient.getName().isEmpty()) {
            name = ingredient.getName().startsWith("lang.")
                    ? LangManager.get(ingredient.getName())
                    : LangManager.asComponent(ingredient.getName());
        }

        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : ingredient.getLore()) {
            loreComponents.add(loreLine.startsWith("lang.")
                    ? LangManager.get(loreLine)
                    : LangManager.asComponent(loreLine));
        }

        GooeyButton.Builder builder = GooeyButton.builder().display(stack);
        
        if (name != null) {
            builder.with(DataComponents.CUSTOM_NAME, name);
        }
        
        if (!loreComponents.isEmpty()) {
            builder.with(DataComponents.LORE, new ItemLore(loreComponents));
        }
        
        // Always hide tooltips for all GUI elements
        builder.with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

        // Add click handler if the item has a command or is otherwise interactive
        if (ingredient.hasCommand()) {
            builder.onClick(action -> handleCustomItemClick(ingredient, player));
        }

        return builder.build();
    }

    /**
     * Creates a dedicated command button
     */
    private static Button createCommandButton(GuiIngredient ingredient, ServerPlayer player, int pageNum) {
        ItemStack stack = ingredient.createItemStack();
        
        // Process name and lore
        Component name = null;
        if (ingredient.getName() != null && !ingredient.getName().isEmpty()) {
            name = ingredient.getName().startsWith("lang.")
                    ? LangManager.get(ingredient.getName())
                    : LangManager.asComponent(ingredient.getName());
        }

        List<Component> loreComponents = new ArrayList<>();
        for (String loreLine : ingredient.getLore()) {
            loreComponents.add(loreLine.startsWith("lang.")
                    ? LangManager.get(loreLine)
                    : LangManager.asComponent(loreLine));
        }

        // Add command info to lore if not hidden
        if (ingredient.hasCommand() && !ingredient.isHideTooltip()) {
            loreComponents.add(Component.literal(""));
            loreComponents.add(LangManager.asComponent("<gray>Click to execute command</gray>"));
        }

        GooeyButton.Builder builder = GooeyButton.builder()
                .display(stack);

        if (name != null) {
            builder.with(DataComponents.CUSTOM_NAME, name);
        } else {
            builder.with(DataComponents.CUSTOM_NAME, LangManager.asComponent("<gold>Command Button</gold>"));
        }

        return builder.with(DataComponents.LORE, new ItemLore(loreComponents))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> handleCustomItemClick(ingredient, player))
                .build();
    }

    /**
     * Handles clicking on custom items and command buttons
     */
    private static void handleCustomItemClick(GuiIngredient ingredient, ServerPlayer player) {
        // Check permission if required
        if (ingredient.getPermission() != null && !ingredient.getPermission().isEmpty()) {
            // Simple permission check - can be enhanced with proper permission system
            if (!player.hasPermissions(2)) { // Requires op level 2 or higher
                String message = ingredient.getNoPermissionMessage();
                if (message != null && !message.isEmpty()) {
                    player.sendSystemMessage(Component.literal(message.replace("&", "§")));
                } else {
                    player.sendSystemMessage(Component.literal("§cYou don't have permission to use this!"));
                }
                return;
            }
        }

        // Execute command if present
        if (ingredient.hasCommand()) {
            executeIngredientCommand(ingredient, player);
        }

        // Show click message if present
        if (ingredient.getClickMessage() != null && !ingredient.getClickMessage().isEmpty()) {
            String message = ingredient.getClickMessage()
                    .replace("%player%", player.getName().getString())
                    .replace("&", "§");
            player.sendSystemMessage(Component.literal(message));
        }
    }

    /**
     * Executes a command from an ingredient
     */
    private static void executeIngredientCommand(GuiIngredient ingredient, ServerPlayer player) {
        String command = ingredient.getCommand();
        if (command == null || command.isEmpty()) {
            return;
        }

        // Replace placeholders
        String processedCommand = command
                .replace("%player%", player.getName().getString())
                .replace("%uuid%", player.getUUID().toString())
                .replace("%x%", String.valueOf((int) player.getX()))
                .replace("%y%", String.valueOf((int) player.getY()))
                .replace("%z%", String.valueOf((int) player.getZ()));

        try {
            MinecraftServer server = player.getServer();
            if (server != null) {
                CommandSourceStack source;
                
                if (ingredient.isExecuteAsPlayer()) {
                    // Execute as player
                    source = player.createCommandSourceStack();
                } else {
                    // Execute as server/console
                    source = server.createCommandSourceStack();
                }

                // Remove leading slash if present
                if (processedCommand.startsWith("/")) {
                    processedCommand = processedCommand.substring(1);
                }

                // Execute the command
                server.getCommands().performPrefixedCommand(source, processedCommand);
                
                CobblePass.LOGGER.info("Executed command '{}' from GUI ingredient for player {}",
                        processedCommand, player.getName().getString());
            }
        } catch (Exception e) {
            CobblePass.LOGGER.error("Failed to execute command '{}' for player {}: {}",
                    processedCommand, player.getName().getString(), e.getMessage());
            player.sendSystemMessage(Component.literal("§cFailed to execute command!"));
        }
    }
}
