# CobblePass

A Battle Pass mod for Cobblemon 1.6+ running on Minecraft 1.21.1 with Fabric. Provides a progression and reward system with both free and premium tiers.

## Features

### Battle Pass System
- Customizable levels of progression
- Free and premium tier rewards
- Dynamic reward configuration
- Direct reward claiming
- Server-side functionality with client UI

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
      "level": 1,
      "freeReward": {
        "type": "COBBLEMON_ITEM",
        "data": {
          "id": "cobblemon:poke_ball",
          "Count": 5
        }
      }
    },
    {
      "level": 2,
      "freeReward": {
        "type": "MINECRAFT_ITEM",
        "data": {
          "id": "minecraft:iron_ingot",
          "Count": 10
        }
      }
    },
    {
      "level": 10,
      "premiumReward": {
        "type": "POKEMON",
        "data": {
          "species": "charmander",
          "level": 15,
          "shiny": true
        }
      }
    }
  ]
}

```

### Reward Types
- `COBBLEMON_ITEM`: Cobblemon-specific items
- `MINECRAFT_ITEM`: Vanilla Minecraft items
- `POKEMON`: Pokémon rewards

## Commands

### Player Commands
- `/battlepass or /bp` - Open the battle pass UI
- `/battlepass premium` - View premium status
- `/battlepass premium buy` - Buy premium pass

### Admin Commands
- `/battlepass addlevels [player] <amount>` - Add levels to a player (max 100)
- `/battlepass reload` - Reload configuration

## Dependencies

### Required
- Minecraft: 1.21.1
- Fabric Loader: 0.16.5
- Fabric API: 0.104.0+1.21.1
- GooeyLibs: 3.1.0-1.21.1-SNAPSHOT
- Cobblemon: 1.6.0+1.21.1
- Impactor Economy API: 5.3.0


## License

All rights reserved.
