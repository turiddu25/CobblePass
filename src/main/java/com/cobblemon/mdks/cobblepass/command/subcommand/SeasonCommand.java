package com.cobblemon.mdks.cobblepass.command.subcommand;

import com.cobblemon.mdks.cobblepass.CobblePass;
import com.cobblemon.mdks.cobblepass.season.*;
import com.cobblemon.mdks.cobblepass.util.Constants;
import com.cobblemon.mdks.cobblepass.util.LangManager;
import com.cobblemon.mdks.cobblepass.util.Subcommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class SeasonCommand extends Subcommand {
    
    // Track pending confirmations and their options
    private static final Map<UUID, SeasonResetOptions> pendingConfirmations = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> confirmationTimeouts = new ConcurrentHashMap<>();
    private static final long CONFIRMATION_TIMEOUT_MS = 30000; // 30 seconds
    
    // Track active progress displays
    private static final Map<UUID, CompletableFuture<?>> activeOperations = new ConcurrentHashMap<>();

    public SeasonCommand() {
        super("§9Usage:\n§3- /battlepass season <start|stop|endseason>");
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("season")
                .requires(source -> source.hasPermission(4)) // Requires operator permission level
                .then(Commands.literal("start")
                        .executes(this::startSeason))
                .then(Commands.literal("stop")
                        .executes(this::stopSeason))
                .then(Commands.literal("endseason")
                        .executes(this::showEndSeasonConfirmation))
                .then(Commands.literal("confirm")
                        .then(Commands.argument("confirmation", StringArgumentType.word())
                                .executes(this::handleConfirmation)))
                .build();
    }

    private int startSeason(CommandContext<CommandSourceStack> context) {
        if (CobblePass.config.isSeasonActive()) {
            context.getSource().sendFailure(LangManager.get(Constants.MSG_SEASON_ALREADY_ACTIVE, CobblePass.config.getCurrentSeason()));
            return 0;
        }

        CobblePass.config.startNewSeason();
        context.getSource().sendSuccess(() -> LangManager.get(Constants.MSG_SEASON_STARTED, CobblePass.config.getCurrentSeason()), false);

        return 1;
    }

    private int stopSeason(CommandContext<CommandSourceStack> context) {
        if (!CobblePass.config.isSeasonActive()) {
            context.getSource().sendFailure(LangManager.get(Constants.MSG_NO_ACTIVE_SEASON));
            return 0;
        }

        CobblePass.config.stopSeason();
        context.getSource().sendSuccess(() -> Component.literal("§aSuccessfully stopped the battle pass season."), false);

        return 1;
    }
    
    private int showEndSeasonConfirmation(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(LangManager.get("lang.command.must_be_player"));
            return 0;
        }
        
        ServerPlayer player = context.getSource().getPlayer();
        
        // Check if season is active
        if (!CobblePass.config.isSeasonActive()) {
            context.getSource().sendFailure(LangManager.get("lang.season.reset.error.no_active_season"));
            return 0;
        }
        
        // Check if another operation is in progress
        SeasonManager seasonManager = SeasonManager.getInstance();
        if (seasonManager.isTransitionInProgress()) {
            context.getSource().sendFailure(LangManager.get("lang.season.reset.error.already_in_progress"));
            return 0;
        }
        
        // Show confirmation GUI
        showSeasonEndConfirmationGUI(player);
        return 1;
    }
    
    private void showSeasonEndConfirmationGUI(ServerPlayer player) {
        // Create default reset options
        SeasonResetOptions defaultOptions = new SeasonResetOptions();
        
        ChestTemplate template = ChestTemplate.builder(6)
                // Title and warning
                .set(1, 4, createInfoButton(
                        Items.BARRIER,
                        LangManager.get("lang.season.reset.confirm.title"),
                        Arrays.asList(
                                LangManager.get("lang.season.reset.confirm.message"),
                                Component.literal(""),
                                LangManager.get("lang.season.reset.confirm.warning"),
                                Component.literal(""),
                                LangManager.get("lang.season.reset.confirm.impact",
                                        Map.of("playerCount", getPlayerCount(), "totalProgress", "all"))
                        )
                ))
                
                // Premium preservation mode selection
                .set(2, 2, createPreservationModeButton(defaultOptions, PremiumPreservationMode.PRESERVE_ALL))
                .set(2, 3, createPreservationModeButton(defaultOptions, PremiumPreservationMode.SYNC_PERMISSIONS))
                .set(2, 4, createPreservationModeButton(defaultOptions, PremiumPreservationMode.PRESERVE_AND_SYNC))
                .set(2, 5, createPreservationModeButton(defaultOptions, PremiumPreservationMode.NONE))
                
                // Options toggles
                .set(3, 2, createToggleButton("Broadcast Messages", defaultOptions.isBroadcastMessages(), 
                        (options, value) -> options.setBroadcastMessages(value)))
                .set(3, 3, createToggleButton("Create Backup", defaultOptions.isCreateBackup(), 
                        (options, value) -> options.setCreateBackup(value)))
                .set(3, 4, createToggleButton("Validate Before Reset", defaultOptions.isValidateBeforeReset(), 
                        (options, value) -> options.setValidateBeforeReset(value)))
                
                // Action buttons
                .set(4, 2, createConfirmButton(player, defaultOptions))
                .set(4, 6, createCancelButton(player))
                
                // Information
                .set(5, 4, createInfoButton(
                        Items.BOOK,
                        Component.literal("§7Information"),
                        Arrays.asList(
                                LangManager.get("lang.season.reset.confirm.backup"),
                                LangManager.get("lang.season.reset.confirm.proceed"),
                                LangManager.get("lang.season.reset.confirm.cancel")
                        )
                ))
                .build();
        
        GooeyPage page = GooeyPage.builder()
                .template(template)
                .title("§cConfirm Season Reset")
                .build();
        
        UIManager.openUIForcefully(player, page);
    }
    
    private Button createInfoButton(net.minecraft.world.item.Item item, Component name, List<Component> lore) {
        return GooeyButton.builder()
                .display(new ItemStack(item))
                .with(DataComponents.CUSTOM_NAME, name)
                .with(DataComponents.LORE, new ItemLore(lore))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .build();
    }
    
    private Button createPreservationModeButton(SeasonResetOptions options, PremiumPreservationMode mode) {
        ItemStack display;
        String name;
        List<String> lore = new ArrayList<>();
        
        switch (mode) {
            case PRESERVE_ALL:
                display = new ItemStack(Items.DIAMOND);
                name = "§aPreserve All Premium";
                lore.add("§7Keep premium status for all");
                lore.add("§7current premium players");
                break;
            case SYNC_PERMISSIONS:
                display = new ItemStack(Items.REDSTONE);
                name = "§eSync from Permissions";
                lore.add("§7Grant premium based on");
                lore.add("§7permission nodes only");
                break;
            case PRESERVE_AND_SYNC:
                display = new ItemStack(Items.EMERALD);
                name = "§bPreserve + Sync";
                lore.add("§7Keep existing premium AND");
                lore.add("§7sync from permissions");
                break;
            case NONE:
                display = new ItemStack(Items.COAL);
                name = "§cNo Preservation";
                lore.add("§7All players lose premium");
                lore.add("§7status on reset");
                break;
            default:
                display = new ItemStack(Items.BARRIER);
                name = "§cUnknown Mode";
                break;
        }
        
        // Add selection indicator
        if (options.getPreservationMode() == mode) {
            lore.add("");
            lore.add("§a✓ Selected");
        } else {
            lore.add("");
            lore.add("§7Click to select");
        }
        
        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(Component.literal(line));
        }
        
        return GooeyButton.builder()
                .display(display)
                .with(DataComponents.CUSTOM_NAME, Component.literal(name))
                .with(DataComponents.LORE, new ItemLore(loreComponents))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    options.setPreservationMode(mode);
                    // Refresh the GUI
                    showSeasonEndConfirmationGUI((ServerPlayer) action.getPlayer());
                })
                .build();
    }
    
    private Button createToggleButton(String optionName, boolean currentValue, ToggleAction action) {
        ItemStack display = currentValue ? new ItemStack(Items.LIME_DYE) : new ItemStack(Items.GRAY_DYE);
        String name = (currentValue ? "§a✓ " : "§c✗ ") + optionName;
        List<String> lore = Arrays.asList(
                currentValue ? "§7Currently enabled" : "§7Currently disabled",
                "§7Click to toggle"
        );
        
        List<Component> loreComponents = new ArrayList<>();
        for (String line : lore) {
            loreComponents.add(Component.literal(line));
        }
        
        return GooeyButton.builder()
                .display(display)
                .with(DataComponents.CUSTOM_NAME, Component.literal(name))
                .with(DataComponents.LORE, new ItemLore(loreComponents))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(clickAction -> {
                    // This would need to be implemented with a more complex state management system
                    // For now, we'll keep the current values
                })
                .build();
    }
    
    private Button createConfirmButton(ServerPlayer player, SeasonResetOptions options) {
        return GooeyButton.builder()
                .display(new ItemStack(Items.GREEN_CONCRETE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§a§lCONFIRM RESET"))
                .with(DataComponents.LORE, new ItemLore(Arrays.asList(
                        Component.literal("§7Click to proceed with"),
                        Component.literal("§7the season reset operation"),
                        Component.literal(""),
                        Component.literal("§c§lWARNING: This cannot be undone!")
                )))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    player.closeContainer();
                    executeSeasonReset(player, options);
                })
                .build();
    }
    
    private Button createCancelButton(ServerPlayer player) {
        return GooeyButton.builder()
                .display(new ItemStack(Items.RED_CONCRETE))
                .with(DataComponents.CUSTOM_NAME, Component.literal("§c§lCANCEL"))
                .with(DataComponents.LORE, new ItemLore(Arrays.asList(
                        Component.literal("§7Click to cancel the"),
                        Component.literal("§7season reset operation")
                )))
                .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                .onClick(action -> {
                    player.closeContainer();
                    player.sendSystemMessage(Component.literal("§7Season reset cancelled."));
                })
                .build();
    }
    
    private void executeSeasonReset(ServerPlayer player, SeasonResetOptions options) {
        UUID playerId = player.getUUID();
        
        // Check if player already has an active operation
        if (activeOperations.containsKey(playerId)) {
            player.sendSystemMessage(LangManager.get("lang.season.reset.error.already_in_progress"));
            return;
        }
        
        // Start the reset operation
        SeasonManager seasonManager = SeasonManager.getInstance();
        
        // Send initial progress message
        player.sendSystemMessage(LangManager.get("lang.season.reset.progress.starting"));
        
        CompletableFuture<SeasonResetResult> resetFuture = seasonManager.endSeason(options);
        activeOperations.put(playerId, resetFuture);
        
        // Start progress tracking
        startProgressTracking(player, resetFuture);
        
        // Handle completion
        resetFuture.whenComplete((result, throwable) -> {
            activeOperations.remove(playerId);
            
            if (throwable != null) {
                CobblePass.LOGGER.error("Season reset failed with exception", throwable);
                player.sendSystemMessage(LangManager.get("lang.season.reset.error.operation_failed",
                        Map.of("error", throwable.getMessage())));
                return;
            }
            
            if (result.isSuccess()) {
                // Send completion summary
                sendResetCompletionSummary(player, result);
            } else {
                player.sendSystemMessage(LangManager.get("lang.season.reset.error.operation_failed",
                        Map.of("error", result.getErrorMessage())));
            }
        });
    }
    
    private void startProgressTracking(ServerPlayer player, CompletableFuture<SeasonResetResult> resetFuture) {
        // This is a simplified progress tracking - in a real implementation,
        // you'd want to have the SeasonManager provide progress updates
        CompletableFuture.runAsync(() -> {
            String[] operations = {
                    "Creating backup...",
                    "Preserving premium status...",
                    "Clearing player progress...",
                    "Restoring premium status...",
                    "Finalizing reset..."
            };
            
            for (int i = 0; i < operations.length && !resetFuture.isDone(); i++) {
                int progress = (i + 1) * 20; // 20% per operation
                
                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("progress", String.valueOf(progress));
                placeholders.put("operation", operations[i]);
                
                player.sendSystemMessage(LangManager.get("lang.season.reset.progress.operation", placeholders));
                
                try {
                    Thread.sleep(2000); // Wait 2 seconds between updates
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
    
    private void sendResetCompletionSummary(ServerPlayer player, SeasonResetResult result) {
        SeasonResetSummary summary = result.getSummary();
        
        // Send main completion message
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("playerCount", String.valueOf(summary.getTotalPlayersReset()));
        placeholders.put("premiumCount", String.valueOf(summary.getPremiumPlayersPreserved()));
        
        player.sendSystemMessage(LangManager.get("lang.season.reset.complete.summary", placeholders));
        
        // Send detailed information
        placeholders.clear();
        placeholders.put("totalPlayers", String.valueOf(summary.getTotalPlayersReset()));
        placeholders.put("premiumPreserved", String.valueOf(summary.getPremiumPlayersPreserved()));
        placeholders.put("backupFiles", String.valueOf(summary.getBackupFilesCreated()));
        
        player.sendSystemMessage(LangManager.get("lang.season.reset.complete.details", placeholders));
        
        // Send operation duration
        long durationSeconds = result.getOperationDuration() / 1000;
        placeholders.clear();
        placeholders.put("duration", String.valueOf(durationSeconds));
        
        player.sendSystemMessage(LangManager.get("lang.season.reset.complete.duration", placeholders));
        
        // Send new season information if available
        if (summary.getNewSeasonId() != null) {
            placeholders.clear();
            placeholders.put("newSeasonId", summary.getNewSeasonId());
            
            player.sendSystemMessage(LangManager.get("lang.season.reset.complete.new_season", placeholders));
        }
    }
    
    private int handleConfirmation(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().isPlayer()) {
            context.getSource().sendFailure(LangManager.get("lang.command.must_be_player"));
            return 0;
        }
        
        ServerPlayer player = context.getSource().getPlayer();
        UUID playerId = player.getUUID();
        String confirmation = StringArgumentType.getString(context, "confirmation");
        
        // Check if player has a pending confirmation
        if (!pendingConfirmations.containsKey(playerId)) {
            player.sendSystemMessage(Component.literal("§cNo pending season reset confirmation."));
            return 0;
        }
        
        // Check timeout
        Long timeoutTime = confirmationTimeouts.get(playerId);
        if (timeoutTime != null && System.currentTimeMillis() > timeoutTime) {
            pendingConfirmations.remove(playerId);
            confirmationTimeouts.remove(playerId);
            player.sendSystemMessage(Component.literal("§cConfirmation timeout expired."));
            return 0;
        }
        
        if ("CONFIRM".equalsIgnoreCase(confirmation)) {
            SeasonResetOptions options = pendingConfirmations.remove(playerId);
            confirmationTimeouts.remove(playerId);
            executeSeasonReset(player, options);
            return 1;
        } else if ("CANCEL".equalsIgnoreCase(confirmation)) {
            pendingConfirmations.remove(playerId);
            confirmationTimeouts.remove(playerId);
            player.sendSystemMessage(Component.literal("§7Season reset cancelled."));
            return 1;
        } else {
            player.sendSystemMessage(Component.literal("§cInvalid confirmation. Type 'CONFIRM' or 'CANCEL'."));
            return 0;
        }
    }
    
    private String getPlayerCount() {
        // This would need to be implemented to count actual players with battle pass data
        return "estimated";
    }
    
    @FunctionalInterface
    private interface ToggleAction {
        void apply(SeasonResetOptions options, boolean value);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        // This command has subcommands, so this method shouldn't be called directly.
        return 0;
    }
}