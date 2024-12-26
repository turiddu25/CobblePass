# CobblePass

A Battle Pass mod for Cobblemon 1.6+ running on Minecraft 1.21.1 with Fabric. Provides a progression and reward system with both free and premium tiers.

## Features

### Battle Pass System
- 100 levels of progression
- Free and premium tier rewards
- Dynamic reward configuration
- Premium exclusive rewards at levels 20 and 30
- Direct reward claiming
- Server-side functionality with client UI

### User Interface
- Clean, intuitive horizontal tier display
- Status indicators showing level progress
- Color-coded reward availability:
  - Gray: Not reached
  - Red: Premium locked
  - Green: Available
- Pagination system for browsing all 100 tiers
- Premium status indicators
- Level and XP progress tracking

### Server Integration
- Operator commands for managing player progress
- Runtime configuration updates
- Premium status tracking
- Impactor integration for premium pass purchases

## Dependencies

- Minecraft: 1.21.1
- Fabric Loader: 0.16.5
- Fabric API: 0.104.0+1.21.1
- GooeyLibs: 3.1.0-1.21.1-SNAPSHOT
- Cobblemon: 1.6.0+1.21.1
- Impactor Economy API: 5.3.0

## Commands

### Player Commands
- `/battlepass view` - Open the battle pass UI
- `/battlepass claim` - Claim available rewards
- `/battlepass premium` - View premium status

### Admin Commands
- `/battlepass addlevels [player] <amount>` - Add levels to a player (max 100)
- `/battlepass reload` - Reload configuration

## Development

### Technical Stack
- Java 21
- Gradle with Kotlin DSL
- GooeyLibs 3.1.0+ for UI components
- Brigadier command system

### Key Patterns

#### UI Components
```java
// Button Creation
GooeyButton button = GooeyButton.builder()
    .display(itemStack)
    .with(DataComponents.CUSTOM_NAME, Component.literal("name"))
    .with(DataComponents.LORE, new ItemLore(loreList))
    .with(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
    .build();

// Page Creation
ChestTemplate template = ChestTemplate.builder(rows)
    .set(x, y, button)
    .build();
```

#### Reward Configuration
```java
private static class TierReward {
    final ItemStack item;
    final boolean isPremium;
}

private static TierReward getRewardForTier(int level) {
    return switch (level) {
        case 20 -> new TierReward(new ItemStack(Items.DIAMOND), true);
        case 1 -> new TierReward(new ItemStack(Items.DIRT), false);
        default -> new TierReward(new ItemStack(Items.DIRT), false);
    };
}
```

## UI Layout

### Top Row
- Experience Bottle (slot 4): Shows level and XP progress
- Premium Status (slot 8): Apple/Golden Apple indicator

### Reward Display
- Horizontal display of tier rewards
- 9 items per page
- Shows actual item names
- Status indicators below each reward

### Navigation
- Previous/Next page arrows for browsing tiers

## Contributing

When contributing, please follow these patterns:

1. UI Components:
   - Use modern GooeyLibs patterns with DataComponents
   - Implement proper pagination for multi-page interfaces
   - Follow established color code standards

2. Commands:
   - Extend Subcommand base class
   - Implement proper permission checks
   - Follow Brigadier command structure

3. Battle Pass Features:
   - Use TierReward system for reward configuration
   - Maintain centralized reward management
   - Follow status indicator patterns

## License

All rights reserved.
