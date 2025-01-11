# CobblePass

A Battle Pass mod for Cobblemon 1.6+ running on Minecraft 1.21.1 with Fabric. Provides a progression and reward system with both free and premium tiers. Players gain XP by catching and defeating pokemon, allowing them to progress through highly customisable tiers of rewards!

PLEASE READ ENTIRE DESCRIPTION FOR FULL USAGE GUIDE! <3
## Installation and Dependencies

[Curseforge download](https://www.curseforge.com/minecraft/mc-mods/cobblemon-cobblepass)

[Modrinth download
](https://modrinth.com/mod/cobble-pass)

This mod has a few dependencies:
- [Cobblemon](https://modrinth.com/mod/cobblemon)
- [Impactor](https://modrinth.com/mod/impactor)
- [GooeyLibs](https://modrinth.com/mod/gooeylibs)

GooeyLibs and Impactor are only needed server-side


## Features

### Battle Pass System
- Customizable levels of progression
- Free and premium tier rewards
- Dynamic reward configuration
- Direct reward claiming
- Server-side functionality with client UI
- Dynamically edit tiers, general config and player data without server reboots

![CobblePass](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExaTU2ZTBpNWpkMXpyOTJyY3FnOWYxZHppOXRpcnhobm5veXU3bjVseCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/YNbbM79OAECuE6HU5I/giphy.gif)
## Configuration

### config.json
```json
{
  "maxLevel": 100,            // Maximum level players can reach
  "xpPerLevel": 1000,         // XP required to level up
  "catchXP": 100,             // XP gained per Pokémon catch
  "defeatXP": 50,             // XP gained per Pokémon defeat
  "premiumCost": 1000,        // Cost in Impactor currency for premium pass
  "seasonDurationDays": 60,   // Duration of each season in days
  "currentSeason": 0,         // Current season number
  "xpMultiplier": 1.1         // XP Multiplier for XP calculation of each level
}
```

### tiers.json
```json
{
  "tiers": [
    {
      "level": 1,                                    // The tier level (required)
      "freeReward": {                               // Free reward that all players can claim
        "type": "ITEM",                             // ITEM type works with any mod's items
        "data": {
          "id": "cobblemon:poke_ball",              // Item ID in format "modid:item_name"
          "Count": 5                                // Number of items to give
        }
      }
    },
    {
      "level": 2,                                   // Each tier must have a unique level
      "premiumReward": {                           // Premium reward only for premium pass holders
        "type": "POKEMON",                         // POKEMON type for Pokemon rewards
        "data": {
          "species": "charmander",                 // Pokemon species name
          "level": 15,                             // Pokemon's level (optional)
          "shiny": true                            // Whether Pokemon is shiny (optional)
        }
      }
    },
    {
      "level": 3,
      "freeReward": {
        "type": "COMMAND",                         // COMMAND type for custom commands
        "command": "effect give %player% minecraft:regeneration 30 2",  // Command to execute (%player% replaced with player name)
        "data": {
          "id": "minecraft:potion",                // Item to show in UI
          "display_name": "Healing Boost"          // Custom name to show in UI (optional)
        }
      }
    }
  ]
}

```
### Edit Player Data
```
{
  "version": "1.0",
  "level": 22,
  "xp": 300,
  "isPremium": false,
  "claimedFreeRewards": [
    1,
    2,
    9
  ],
  "claimedPremiumRewards": []
}
```

### Reward Types
- `ITEM`: Any type of Item
- `COMMAND`: Command execution reward
- `POKEMON`: Pokémon rewards

## Commands

### Player Commands
- `/battlepass or /bp` - Open the battle pass UI
- `/battlepass premium` - View premium status
- `/battlepass premium buy` - Buy premium pass

### Admin Commands
- `/battlepass start` - Starts the battlepass season
- `/battlepass addlevels [player] <amount>` - Add levels to a player 
- `/battlepass addxp [player] <amount>` - Add xp to a player
- `/battlepass reload` - Reload configuration
