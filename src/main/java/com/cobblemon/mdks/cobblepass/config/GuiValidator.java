package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive validator for GUI structure integrity.
 * Validates structure dimensions, required elements, and ingredient definitions.
 */
public class GuiValidator {
    private static final int GUI_ROWS = 6;
    private static final int GUI_COLS = 9;
    
    /**
     * Performs comprehensive validation of a GUI structure and its ingredients.
     * 
     * @param structure The GUI structure to validate
     * @param ingredients The ingredient definitions
     * @return ValidationResult containing all validation findings
     */
    public static ValidationResult validateComplete(List<String> structure, Map<Character, GuiIngredient> ingredients) {
        ValidationResult result = new ValidationResult();
        
        // Validate structure dimensions
        validateDimensions(structure, result);
        
        // Validate required placeholder types
        validateRequiredPlaceholders(structure, result);
        
        // Validate navigation elements
        validateNavigationElements(structure, result);
        
        // Validate ingredient definitions
        validateIngredients(structure, ingredients, result);
        
        // Validate structure balance and layout
        validateLayout(structure, result);
        
        return result;
    }
    
    /**
     * Validates that the structure has correct dimensions.
     */
    private static void validateDimensions(List<String> structure, ValidationResult result) {
        if (structure == null) {
            result.addError("Structure cannot be null");
            return;
        }
        
        if (structure.size() != GUI_ROWS) {
            result.addError("Structure must have exactly " + GUI_ROWS + " rows, found " + structure.size());
        }
        
        for (int i = 0; i < structure.size(); i++) {
            String row = structure.get(i);
            if (row == null) {
                result.addError("Row " + i + " is null");
            } else if (row.length() != GUI_COLS) {
                result.addError("Row " + i + " must be exactly " + GUI_COLS + " characters long, found " + row.length() + " characters");
            }
        }
    }
    
    /**
     * Validates that all required placeholder types are present.
     */
    private static void validateRequiredPlaceholders(List<String> structure, ValidationResult result) {
        if (structure == null) return;
        
        boolean hasRewardSlots = false;
        boolean hasFreeRewards = false;
        boolean hasPremiumRewards = false;
        
        for (String row : structure) {
            if (row != null) {
                if (row.contains("f")) {
                    hasFreeRewards = true;
                    hasRewardSlots = true;
                }
                if (row.contains("r")) {
                    hasPremiumRewards = true;
                    hasRewardSlots = true;
                }
            }
        }
        
        if (!hasRewardSlots) {
            result.addError("No reward slots found - structure must contain 'f' (free rewards) or 'r' (premium rewards)");
        } else {
            if (!hasFreeRewards) {
                result.addWarning("No free reward slots ('f') found - players may not be able to claim free rewards");
            }
            if (!hasPremiumRewards) {
                result.addWarning("No premium reward slots ('r') found - premium players may not be able to claim premium rewards");
            }
        }
    }
    
    /**
     * Validates navigation elements positioning and presence.
     */
    private static void validateNavigationElements(List<String> structure, ValidationResult result) {
        if (structure == null) return;
        
        boolean hasPrevious = false;
        boolean hasNext = false;
        int navigationCount = 0;
        
        for (int rowIndex = 0; rowIndex < structure.size(); rowIndex++) {
            String row = structure.get(rowIndex);
            if (row != null) {
                for (int colIndex = 0; colIndex < row.length(); colIndex++) {
                    char c = row.charAt(colIndex);
                    if (c == '<') {
                        hasPrevious = true;
                        navigationCount++;
                        // Validate positioning - navigation should typically be on bottom row
                        if (rowIndex != GUI_ROWS - 1) {
                            result.addWarning("Previous navigation ('<') found on row " + rowIndex + " - typically should be on bottom row (" + (GUI_ROWS - 1) + ")");
                        }
                    } else if (c == '>') {
                        hasNext = true;
                        navigationCount++;
                        // Validate positioning - navigation should typically be on bottom row
                        if (rowIndex != GUI_ROWS - 1) {
                            result.addWarning("Next navigation ('>') found on row " + rowIndex + " - typically should be on bottom row (" + (GUI_ROWS - 1) + ")");
                        }
                    }
                }
            }
        }
        
        if (!hasPrevious && !hasNext) {
            result.addWarning("No navigation elements found - players may not be able to navigate between tiers");
        } else if (!hasPrevious) {
            result.addWarning("No previous navigation ('<') found - players may not be able to go to previous tiers");
        } else if (!hasNext) {
            result.addWarning("No next navigation ('>') found - players may not be able to go to next tiers");
        }
        
        if (navigationCount > 2) {
            result.addWarning("Multiple navigation elements found (" + navigationCount + ") - this may cause confusion");
        }
    }
    
    /**
     * Validates ingredient definitions against the structure.
     */
    private static void validateIngredients(List<String> structure, Map<Character, GuiIngredient> ingredients, ValidationResult result) {
        if (structure == null) return;
        
        // Collect all characters used in structure
        Map<Character, Integer> usedCharacters = new HashMap<>();
        for (int rowIndex = 0; rowIndex < structure.size(); rowIndex++) {
            String row = structure.get(rowIndex);
            if (row != null) {
                for (int colIndex = 0; colIndex < row.length(); colIndex++) {
                    char c = row.charAt(colIndex);
                    usedCharacters.put(c, usedCharacters.getOrDefault(c, 0) + 1);
                }
            }
        }
        
        // Check for missing ingredient definitions
        for (Character c : usedCharacters.keySet()) {
            if (c != ' ' && (ingredients == null || !ingredients.containsKey(c))) {
                result.addError("No ingredient definition found for character '" + c + "' (used " + usedCharacters.get(c) + " times)");
            }
        }
        
        // Check for unused ingredient definitions
        if (ingredients != null) {
            for (Character c : ingredients.keySet()) {
                if (!usedCharacters.containsKey(c)) {
                    result.addWarning("Ingredient definition for character '" + c + "' is defined but not used in structure");
                }
            }
        }
        
        // Validate individual ingredients
        if (ingredients != null) {
            for (Map.Entry<Character, GuiIngredient> entry : ingredients.entrySet()) {
                char character = entry.getKey();
                GuiIngredient ingredient = entry.getValue();
                
                if (ingredient == null) {
                    result.addError("Ingredient definition for character '" + character + "' is null");
                    continue;
                }
                
                // Validate ingredient properties
                validateIngredient(character, ingredient, result);
            }
        }
    }
    
    /**
     * Validates an individual ingredient definition.
     */
    private static void validateIngredient(char character, GuiIngredient ingredient, ValidationResult result) {
        // Validate material
        String material = ingredient.getMaterial();
        if (material == null || material.trim().isEmpty()) {
            // Only warn for non-placeholder types that should have materials
            if (ingredient.getType() == GuiIngredient.IngredientType.STATIC_ITEM) {
                result.addWarning("Ingredient '" + character + "' has no material defined");
            }
        }
        
        // Validate ingredient type
        if (ingredient.getType() == null) {
            result.addError("Ingredient '" + character + "' has no type defined");
        }
        
        // Validate specific ingredient types
        switch (ingredient.getType()) {
            case FREE_REWARD_PLACEHOLDER:
                if (character != 'f') {
                    result.addWarning("Free reward placeholder typically uses character 'f', but found '" + character + "'");
                }
                break;
            case PREMIUM_REWARD_PLACEHOLDER:
                if (character != 'r') {
                    result.addWarning("Premium reward placeholder typically uses character 'r', but found '" + character + "'");
                }
                break;
            case NAVIGATION_PREVIOUS:
                if (character != '<') {
                    result.addWarning("Previous navigation typically uses character '<', but found '" + character + "'");
                }
                break;
            case NAVIGATION_NEXT:
                if (character != '>') {
                    result.addWarning("Next navigation typically uses character '>', but found '" + character + "'");
                }
                break;
        }
    }
    
    /**
     * Validates overall layout balance and design.
     */
    private static void validateLayout(List<String> structure, ValidationResult result) {
        if (structure == null) return;
        
        // Count different types of elements
        int borderCount = 0;
        int emptyCount = 0;
        int rewardCount = 0;
        int functionalCount = 0;
        
        for (String row : structure) {
            if (row != null) {
                for (char c : row.toCharArray()) {
                    switch (c) {
                        case '#':
                            borderCount++;
                            break;
                        case ' ':
                        case '-':
                            emptyCount++;
                            break;
                        case 'f':
                        case 'r':
                            rewardCount++;
                            break;
                        case '<':
                        case '>':
                        case 'i':
                        case 'B':
                        case 'P':
                        case 'L':
                        case 'M':
                            functionalCount++;
                            break;
                    }
                }
            }
        }
        
        int totalSlots = GUI_ROWS * GUI_COLS;
        
        // Validate layout balance
        if (rewardCount == 0) {
            result.addError("No reward slots found in layout");
        } else if (rewardCount < 3) {
            result.addWarning("Very few reward slots (" + rewardCount + ") - consider adding more for better progression");
        } else if (rewardCount > 20) {
            result.addWarning("Many reward slots (" + rewardCount + ") - this may make the GUI cluttered");
        }
        
        if (borderCount > totalSlots * 0.6) {
            result.addWarning("Layout has many border elements (" + borderCount + "/" + totalSlots + ") - consider reducing for more functional space");
        }
        
        if (functionalCount == 0) {
            result.addWarning("No functional elements (navigation, info, etc.) found - players may have limited interaction");
        }
    }
    
    /**
     * Generates detailed error messages with specific line numbers and issues.
     * 
     * @param result The validation result to format
     * @return Formatted error report
     */
    public static String generateErrorReport(ValidationResult result) {
        StringBuilder report = new StringBuilder();
        
        if (!result.isValid()) {
            report.append("=== GUI Structure Validation Errors ===\n");
            for (int i = 0; i < result.getErrors().size(); i++) {
                report.append("Error ").append(i + 1).append(": ").append(result.getErrors().get(i)).append("\n");
            }
        }
        
        if (result.hasWarnings()) {
            if (report.length() > 0) report.append("\n");
            report.append("=== GUI Structure Validation Warnings ===\n");
            for (int i = 0; i < result.getWarnings().size(); i++) {
                report.append("Warning ").append(i + 1).append(": ").append(result.getWarnings().get(i)).append("\n");
            }
        }
        
        if (report.length() == 0) {
            report.append("GUI structure validation passed successfully!");
        }
        
        return report.toString();
    }
    
    /**
     * Provides step-by-step recovery instructions for common problems.
     * 
     * @param result The validation result to generate instructions for
     * @return Recovery instructions
     */
    public static String generateRecoveryInstructions(ValidationResult result) {
        StringBuilder instructions = new StringBuilder();
        
        if (!result.isValid() || result.hasWarnings()) {
            instructions.append("=== GUI Structure Recovery Instructions ===\n\n");
            
            // Check for common issues and provide specific instructions
            for (String error : result.getErrors()) {
                if (error.contains("must have exactly")) {
                    instructions.append("• Structure Dimension Issue:\n");
                    instructions.append("  1. Ensure your structure array has exactly 6 rows\n");
                    instructions.append("  2. Each row must be exactly 9 characters long\n");
                    instructions.append("  3. Use '#' for borders, ' ' for empty slots\n\n");
                } else if (error.contains("No reward slots")) {
                    instructions.append("• Missing Reward Slots:\n");
                    instructions.append("  1. Add 'f' characters for free reward slots\n");
                    instructions.append("  2. Add 'r' characters for premium reward slots\n");
                    instructions.append("  3. Typical layout: row 2 for free, row 3 for premium\n\n");
                } else if (error.contains("No ingredient definition")) {
                    instructions.append("• Missing Ingredient Definitions:\n");
                    instructions.append("  1. Define ingredients for all characters used in structure\n");
                    instructions.append("  2. Check the 'ingredients' section in gui.json\n");
                    instructions.append("  3. Each character needs a corresponding ingredient definition\n\n");
                }
            }
            
            for (String warning : result.getWarnings()) {
                if (warning.contains("No navigation elements")) {
                    instructions.append("• Missing Navigation:\n");
                    instructions.append("  1. Add '<' for previous navigation (typically bottom-left)\n");
                    instructions.append("  2. Add '>' for next navigation (typically bottom-right)\n");
                    instructions.append("  3. Example bottom row: '<#######>'\n\n");
                }
            }
            
            instructions.append("• General Recovery Steps:\n");
            instructions.append("  1. Backup your current gui.json file\n");
            instructions.append("  2. Delete gui.json to regenerate defaults\n");
            instructions.append("  3. Restart the server to apply changes\n");
            instructions.append("  4. Customize the new default structure as needed\n");
        }
        
        return instructions.toString();
    }
    
    /**
     * Result of comprehensive GUI validation.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public int getErrorCount() {
            return errors.size();
        }
        
        public int getWarningCount() {
            return warnings.size();
        }
    }
}