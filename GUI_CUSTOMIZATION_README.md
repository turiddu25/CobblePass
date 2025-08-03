# CobblePass GUI Customization & Localization Guide

## Overview

CobblePass now features a completely customizable GUI system and full localization support. Server administrators can design their own Battle Pass interface layouts and customize all text displayed to players.

## Features

- **Fully Customizable Layout**: Design any 6x9 chest GUI layout using a simple grid system
- **Complete Localization**: All text can be translated and customized via `lang.json`
- **Dynamic Placeholders**: Special placeholders automatically populate with battle pass content
- **Multiple Layout Support**: Switch between different layouts by replacing `gui.json`
- **Fallback System**: Robust error handling with automatic fallbacks to defaults

## Configuration Files

### `config/cobblepass/gui.json`
Controls the visual layout and structure of the Battle Pass GUI.

### `config/cobblepass/lang.json`
Contains all translatable text strings used throughout the mod.

## GUI Layout System

### Structure Grid
The GUI uses a 6x9 character grid where each character represents a specific item or placeholder:

```json
{
  "structure": [
    "# i # # B # # P #",
    "# # # # # # # # #", 
    "# L f f f f f L #",
    "# # s s s s s # #",
    "# M r r r r r M #",
    "< # # # # # # # >"
  ]
}
```

### Character Meanings
Each character in the grid corresponds to an ingredient that defines what appears in that slot:

- `#` = Border decoration (glass panes)
- `-` = Empty space (air)
- `i` = XP Info button
- `B` = Battle Pass progress info
- `P` = Premium status button
- `L` = Free rewards label
- `M` = Premium rewards label
- `f` = Free reward slot
- `r` = Premium reward slot
- `s` = Status indicator (claimed/available)
- `<` = Previous page button
- `>` = Next page button

### Ingredient Definitions
Each character is defined in the `ingredients` section:

```json
{
  "ingredients": {
    "#": {
      "type": "STATIC_ITEM",
      "material": "minecraft:light_gray_stained_glass_pane",
      "name": "lang.gui.decoration.border",
      "lore": [],
      "customModelData": 0,
      "hideTooltip": true
    }
  }
}
```

### Placeholder Types

#### Static Items
- `STATIC_ITEM`: Regular items with fixed properties

#### Dynamic Placeholders
- `FREE_REWARD_PLACEHOLDER`: Automatically populated with free rewards
- `PREMIUM_REWARD_PLACEHOLDER`: Automatically populated with premium rewards  
- `STATUS_PLACEHOLDER`: Shows claim status (green/orange/gray indicators)
- `XP_INFO_PLACEHOLDER`: Displays XP earning information
- `PROGRESS_PLACEHOLDER`: Shows current level and XP progress
- `PREMIUM_STATUS_PLACEHOLDER`: Displays premium pass status
- `NAVIGATION_PREVIOUS`: Previous page button
- `NAVIGATION_NEXT`: Next page button
- `FREE_REWARDS_LABEL`: "Free Rewards" header
- `PREMIUM_REWARDS_LABEL`: "Premium Rewards" header

## Example Layouts

### Default Layout (Current)
```
# i # # B # # P #
# # # # # # # # #
# L f f f f f L #
# # s s s s s # #  
# M r r r r r M #
< # # # # # # # >
```

### Centered Layout (Alternative)
```
# # # # i # # # #
# # f f B f f # #
# # s s P s s # #
# # r r - r r # #
# # t t - t t # #
< # # # # # # # >
```

### Compact Layout (Example)
```
i B P # # # # # #
f f f f f # # < >
s s s s s # # # #
r r r r r # # # #
# # # # # # # # #
# # # # # # # # #
```

## Localization System

### Language Keys
All text uses keys that start with `lang.`:

```json
{
  "lang.gui.title": "§3Battle Pass",
  "lang.command.level_up": "§a[BattlePass] §fYou reached level §e%d§f!",
  "lang.gui.reward.free": "§aFree Reward"
}
```

### Key Categories

#### Commands (`lang.command.*`)
- Player messages and feedback
- Error messages
- Success notifications

#### GUI Elements (`lang.gui.*`)
- Button names and descriptions
- Interface text
- Navigation labels

#### Rewards (`lang.reward.*`)
- Reward type descriptions
- Item information
- Status indicators

#### Seasons (`lang.season.*`)
- Season management messages
- Time remaining notifications

### String Formatting
Many keys support placeholders for dynamic values:
- `%d` = Numbers (levels, XP amounts)
- `%s` = Strings (time remaining, names)

Example:
```json
"lang.command.reward_claim": "§a[BattlePass] §fYou claimed the reward for level §e%d§f!"
```

## How to Customize

### 1. GUI Layout Customization

1. **Backup Current Configuration**: Copy `gui.json` to `gui_backup.json`

2. **Edit Structure**: Modify the `structure` array to change the layout:
   ```json
   "structure": [
     "# # # i B p # # #",
     "# f f f f f f f #",
     "# s s s s s s s #", 
     "# r r r r r r r #",
     "# # # # # # # # #",
     "< # # # # # # # >"
   ]
   ```

3. **Update Ingredients**: Ensure all characters used in the structure have corresponding ingredient definitions

4. **Test Layout**: Restart server and use `/bp` to view the new layout

### 2. Text Localization

1. **Edit lang.json**: Modify any text string:
   ```json
   {
     "lang.gui.title": "§6My Custom Battle Pass",
     "lang.command.level_up": "§b🎉 Level §e%d§b achieved! 🎉"
   }
   ```

2. **Use Color Codes**: Minecraft color codes (§) are supported
   - `§a` = Green
   - `§c` = Red  
   - `§e` = Yellow
   - `§b` = Aqua
   - `§6` = Gold

3. **Reload**: Use `/battlepass reload` to apply changes

### 3. Creating Theme Variants

Create multiple GUI files for different themes:
- `gui_default.json` - Standard layout
- `gui_compact.json` - Space-efficient design
- `gui_premium.json` - Fancy layout for special events

Switch themes by copying the desired file to `gui.json` and reloading.

## Troubleshooting

### Missing Translations
If a translation key is missing, the system will display:
```
§c[MISSING] §fFriendly Key Name
```
Add the missing key to `lang.json` to fix.

### GUI Layout Issues
- Ensure all characters in the structure have ingredient definitions
- Check that essential placeholders (reward slots) are present
- Verify JSON syntax is valid

### Reload Commands
- `/battlepass reload` - Reloads all configurations
- Restart server for guaranteed full reload

## Advanced Tips

### Custom Materials
Use any Minecraft or mod item as decoration:
```json
{
  "material": "cobblemon:poke_ball",
  "name": "§ePokémon Theme!",
  "customModelData": 1234
}
```

### Dynamic Lore
Reference other language keys in lore:
```json
{
  "lore": [
    "lang.gui.custom.description.line1",
    "lang.gui.custom.description.line2"
  ]
}
```

### Performance Considerations
- Avoid too many unique items in a single view
- Use air blocks (`minecraft:air`) for spacing instead of glass when possible
- Keep structure arrays exactly 6 rows of 9 characters each

## Support

If you experience issues:
1. Check server logs for error messages
2. Verify JSON syntax with an online validator
3. Test with default configurations to isolate problems
4. Backup your custom configurations before major changes

The system is designed to be robust - if any configuration fails to load, it will automatically fall back to working defaults while logging the error for you to fix.