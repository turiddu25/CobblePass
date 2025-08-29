# CobblePass - Battle Pass System for Cobblemon

A comprehensive battle pass system designed specifically for Cobblemon servers, featuring customizable tiers, rewards, GUI layouts, and premium functionality.

## 🚀 Quick Start

1. **Installation**: Place the mod JAR in your server's `mods` folder
2. **First Run**: Start your server to generate default configuration files
 IMPORTANT: run `/bp create` and `/bp season start` for smooth sailing initially.
3. **Configuration**: Edit files in `config/cobblepass/` to customize your battle pass
4. **Commands**: Use `/battlepass` to open the GUI or `/battlepass help` for admin commands

## 📁 Configuration Files

### Core Files
- **`config.json`** - Main settings (XP values, seasons, premium config)
- **`tiers.json`** - Battle pass tiers and rewards
- **`gui.json`** - GUI layout and appearance
- **`lang.json`** - All text and messages (supports MiniMessage formatting)

## 🎯 Tier Configuration (`tiers.json`)

### Basic Structure

```json
{
  "templates": {
    // Reusable reward templates
  },
  "tiers": [
    // Individual tier definitions
  ]
}
```

### Complete Tier Example

Here's a comprehensive example showing all features:

```json
{
  "templates": {
    "rare_candy_template": {
      "type": "ITEM",
      "data": {
        "id": "cobblemon:rare_candy",
        "Count": 1
      },
      "lockedDisplay": {
        "id": "minecraft:red_stained_glass_pane",
        "display_name": "§cLocked Rare Candy"
      },
      "claimedDisplay": {
        "id": "minecraft:lime_stained_glass_pane",
        "display_name": "§aRare Candy Claimed!"
      },
      "claimableDisplay": {
        "id": "cobblemon:rare_candy",
        "display_name": "§e⭐ Click to Claim Rare Candy!"
      }
    },
    "shiny_pokemon_template": {
      "type": "POKEMON",
      "data": {
        "species": "eevee",
        "shiny": true,
        "level": 25
      },
      "lockedDisplay": {
        "id": "minecraft:orange_stained_glass_pane",
        "display_name": "§6🔒 Locked Shiny Eevee"
      },
      "claimedDisplay": {
        "id": "minecraft:purple_stained_glass_pane",
        "display_name": "§dShiny Eevee Claimed!"
      }
    }
  },
  "tiers": [
    {
      "level": 1,
      "freeReward": {
        "type": "ITEM",
        "data": {
          "id": "cobblemon:poke_ball",
          "Count": 5
        },
        "lockedDisplay": {
          "id": "minecraft:gray_stained_glass_pane",
          "display_name": "§7🔒 Reach Level 1"
        },
        "claimedDisplay": {
          "id": "minecraft:green_wool",
          "display_name": "§a✓ Poke Balls Claimed"
        },
        "claimableDisplay": {
          "id": "cobblemon:poke_ball",
          "display_name": "§e⭐ 5 Poke Balls - Click to Claim!"
        }
      }
    },
    {
      "level": 2,
      "freeReward": {
        "type": "ITEM",
        "data": {
          "id": "minecraft:iron_ingot",
          "Count": 10
        }
      },
      "premiumReward": {
        "$template": "rare_candy_template"
      }
    },
    {
      "level": 5,
      "freeReward": {
        "type": "ITEM",
        "data": {
          "id": "cobblemon:great_ball",
          "Count": 3
        }
      },
      "premiumReward": {
        "type": "ITEM",
        "data": {
          "id": "cobblemon:evolution_stone",
          "Count": 1
        },
        "lockedDisplay": {
          "id": "minecraft:barrier",
          "display_name": "§c🔒 Premium Required"
        },
        "claimedDisplay": {
          "id": "minecraft:emerald_block",
          "display_name": "§a✓ Evolution Stone Claimed"
        }
      }
    },
    {
      "level": 10,
      "premiumReward": {
        "$template": "shiny_pokemon_template"
      }
    }
  ]
}
```

### Reward Types

#### 1. Item Rewards
```json
{
  "type": "ITEM",
  "data": {
    "id": "cobblemon:master_ball",
    "Count": 1
  }
}
```

#### 2. Pokémon Rewards
```json
{
  "type": "POKEMON",
  "data": {
    "species": "pikachu",
    "shiny": true,
    "level": 50,
    "ability": "static"
  }
}
```

#### 3. Command Rewards
```json
{
  "type": "COMMAND",
  "command": "give %player% minecraft:diamond 10",
  "data": {
    "id": "minecraft:diamond",
    "display_name": "§b10 Diamonds"
  }
}
```

### Display States

Each reward can have three different display states:

#### Locked State (Player hasn't reached the level)
```json
"lockedDisplay": {
  "id": "minecraft:gray_stained_glass_pane",
  "display_name": "§7🔒 Reach Level 5"
}
```

#### Claimable State (Player can claim the reward)
```json
"claimableDisplay": {
  "id": "cobblemon:poke_ball",
  "display_name": "§e⭐ 5 Poke Balls - Click to Claim!"
}
```

#### Claimed State (Player has already claimed)
```json
"claimedDisplay": {
  "id": "minecraft:green_wool",
  "display_name": "§a✓ Reward Claimed"
}
```

### Templates System

Templates allow you to reuse reward configurations:

```json
{
  "templates": {
    "pokeball_bundle": {
      "type": "ITEM",
      "data": {
        "id": "cobblemon:poke_ball",
        "Count": 10
      },
      "lockedDisplay": {
        "id": "minecraft:gray_stained_glass_pane",
        "display_name": "§7Locked Poke Balls"
      },
      "claimedDisplay": {
        "id": "minecraft:green_stained_glass_pane",
        "display_name": "§aPoke Balls Claimed"
      }
    }
  },
  "tiers": [
    {
      "level": 3,
      "freeReward": {
        "$template": "pokeball_bundle"
      }
    },
    {
      "level": 7,
      "premiumReward": {
        "$template": "pokeball_bundle"
      }
    }
  ]
}
```

## ⚙️ Main Configuration (`config.json`)

### XP Settings
```json
{
  "maxLevel": 50,
  "catchXP": 100,
  "defeatXP": 50,
  "evolveXP": 75,
  "hatchXP": 50,
  "tradeXP": 25,
  "fishXP": 20,
  "catchLegendaryXP": 500,
  "catchShinyXP": 250,
  "catchUltraBeastXP": 300,
  "catchMythicalXP": 400,
  "catchParadoxXP": 200,
  "releaseXP": 10
}
```

### Season Configuration
```json
{
  "seasonDurationDays": 60,
  "currentSeason": 1,
  "seasonStartTime": 1234567890000,
  "seasonEndTime": 1234567890000
}
```

### XP Progression
```json
{
  "xpProgression": {
    "mode": "FORMULA",
    "xpPerLevel": 1000,
    "xpMultiplier": 1.1,
    "manualXpValues": {}
  }
}
```

For manual XP values per level:
```json
{
  "xpProgression": {
    "mode": "MANUAL",
    "manualXpValues": {
      "1": 100,
      "2": 250,
      "3": 500,
      "4": 1000,
      "5": 2000
    }
  }
}
```

### Premium Configuration
```json
{
  "premiumConfig": {
    "mode": "permission",
    "permissionNode": "cobblepass.premium",
    "economyEnabled": true,
    "premiumCost": 1000,
    "autoRenew": false,
    "preserveOnSeasonChange": true
  }
}
```

## 🎨 GUI Customization (`gui.json`)

### Basic Structure
```json
{
  "title": "lang.gui.title",
  "enableCustomGui": true,
  "structure": {
    "structure": [
      "i   B   P",
      "         ",
      "#L fffff #",
      "#M rrrrr #",
      "         ",
      "<   #   >"
    ],
    "ingredients": {
      // Character definitions
    }
  }
}
```

### GUI Characters

- `i` - XP Info Panel
- `B` - Progress Display
- `P` - Premium Status
- `L` - Free Rewards Label
- `M` - Premium Rewards Label
- `f` - Free Reward Slots
- `r` - Premium Reward Slots
- `<` - Previous Page
- `>` - Next Page
- `#` - Border/Decoration
- ` ` - Empty Space

### Custom GUI Example
```json
{
  "structure": [
    "####i####",
    "#       #",
    "#L fffff #",
    "#M rrrrr #",
    "#   B   #",
    "<#  P  #>"
  ],
  "ingredients": {
    "#": {
      "type": "STATIC_ITEM",
      "material": "minecraft:black_stained_glass_pane",
      "name": " ",
      "hideTooltip": true
    },
    "i": {
      "type": "XP_INFO_PLACEHOLDER",
      "material": "minecraft:experience_bottle",
      "name": "lang.gui.info.title"
    }
  }
}
```

## 🌐 Language Configuration (`lang.json`)

### MiniMessage Support
The mod supports MiniMessage formatting for rich text:

```json
{
  "lang.command.level_up": "<green>[BattlePass]</green> You reached level <yellow>%d</yellow>!",
  "lang.gui.tier_title": "<bold>Tier %d</bold>",
  "lang.reward.item_format": "<white>%s <gray>x%d</gray>",
  "lang.gui.premium.name": "<gold>Premium Pass</gold>"
}
```

### Color Codes
- `<red>`, `<green>`, `<blue>`, `<yellow>`, `<aqua>`, `<white>`, `<gray>`, `<black>`
- `<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`
- `<#FF5555>` for hex colors

## 🎮 Commands

### Player Commands
- `/battlepass` - Open the battle pass GUI
- `/battlepass claim <level>` - Claim a specific reward
- `/battlepass premium` - Purchase premium access (if economy enabled)

### Admin Commands
- `/battlepass reload` - Reload all configuration files
- `/battlepass season start` - Start a new season
- `/battlepass season stop` - End the current season
- `/battlepass season reset` - Reset the current season
- `/battlepass addxp <player> <amount>` - Add XP to a player
- `/battlepass addlevels <player> <levels>` - Add levels to a player
- `/battlepass managepremium <player> <grant|revoke>` - Manage premium status

## 🔧 Advanced Features

### Season Management
```bash
# Start a new 30-day season
/battlepass season start 30

# Reset current season with premium preservation
/battlepass season reset --preserve-premium

# Check season status
/battlepass season status
```

### Premium Integration
- **Permission-based**: Grant `cobblepass.premium` permission
- **Economy-based**: Players purchase with in-game currency
- **Command-based**: Admins grant manually

### Templates and Inheritance
Use templates to maintain consistency across tiers:

```json
{
  "templates": {
    "common_pokeball": {
      "type": "ITEM",
      "data": {"id": "cobblemon:poke_ball", "Count": 5},
      "lockedDisplay": {"id": "minecraft:gray_stained_glass_pane"},
      "claimedDisplay": {"id": "minecraft:green_stained_glass_pane"}
    }
  }
}
```

## 🐛 Troubleshooting

### Common Issues

1. **XP not showing correctly**: Ensure you're using `XP_INFO_PLACEHOLDER` type in GUI
2. **Rewards not displaying**: Check tier level numbers are sequential
3. **Premium not working**: Verify permission nodes or economy setup
4. **GUI layout broken**: Validate structure dimensions (6 rows × 9 columns)

### Debug Commands
```bash
/battlepass reload  # Reload configs
/battlepass season status  # Check season state
```

## 📝 Examples

### Simple 5-Tier Setup
```json
{
  "tiers": [
    {"level": 1, "freeReward": {"type": "ITEM", "data": {"id": "cobblemon:poke_ball", "Count": 5}}},
    {"level": 2, "freeReward": {"type": "ITEM", "data": {"id": "minecraft:iron_ingot", "Count": 10}}},
    {"level": 3, "premiumReward": {"type": "ITEM", "data": {"id": "cobblemon:rare_candy", "Count": 1}}},
    {"level": 4, "freeReward": {"type": "ITEM", "data": {"id": "cobblemon:great_ball", "Count": 3}}},
    {"level": 5, "premiumReward": {"type": "POKEMON", "data": {"species": "eevee", "shiny": true}}}
  ]
}
```

### Mixed Reward Tier
```json
{
  "level": 10,
  "freeReward": {
    "type": "ITEM",
    "data": {"id": "cobblemon:ultra_ball", "Count": 2}
  },
  "premiumReward": {
    "type": "POKEMON",
    "data": {
      "species": "dragonite",
      "level": 50,
      "shiny": false,
      "ability": "multiscale"
    }
  }
}
```

## 🤝 Support

For issues, suggestions, or contributions, please visit the [GitHub repository](https://github.com/turiddu25/CobblePass).

---

**Note**: This mod requires Cobblemon and Fabric to function properly. Ensure all dependencies are installed and up to date.
