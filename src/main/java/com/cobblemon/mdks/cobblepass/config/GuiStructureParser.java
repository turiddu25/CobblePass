package com.cobblemon.mdks.cobblepass.config;

import com.cobblemon.mdks.cobblepass.CobblePass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parser for GUI structure strings.
 * Ensures all structure rows are valid and of the correct length.
 */
public class GuiStructureParser {
    private static final int GUI_ROWS = 6;
    private static final int GUI_COLS = 9;

    /**
     * Parses and validates a structure array.
     *
     * @param structureArray The raw structure array from configuration.
     * @return A validated and normalized structure array.
     */
    public static List<String> parse(List<String> structureArray) {
        if (structureArray == null || structureArray.isEmpty()) {
            CobblePass.LOGGER.warn("Empty structure array provided, generating default");
            return generateDefaultCompactStructure();
        }

        List<String> parsedStructure = new ArrayList<>();

        for (int i = 0; i < GUI_ROWS; i++) {
            String row;
            if (i < structureArray.size()) {
                row = structureArray.get(i);
            } else {
                // Fill missing rows with empty spaces
                row = " ".repeat(GUI_COLS);
                CobblePass.LOGGER.warn("Missing row " + i + " in structure, using empty row");
            }

            String parsedRow = validateAndFixRow(row, i);
            parsedStructure.add(parsedRow);
        }

        return parsedStructure;
    }

    /**
     * Validates and fixes a row to ensure it's exactly 9 characters.
     *
     * @param row The row string.
     * @param rowIndex The row index for error reporting.
     * @return A fixed row.
     */
    private static String validateAndFixRow(String row, int rowIndex) {
        if (row == null) {
            CobblePass.LOGGER.warn("Null row at index " + rowIndex + ", using default empty row");
            return " ".repeat(GUI_COLS);
        }

        if (row.length() == GUI_COLS) {
            return validateCharacters(row, rowIndex);
        } else if (row.length() < GUI_COLS) {
            // Pad with spaces
            String paddedRow = row + " ".repeat(GUI_COLS - row.length());
            CobblePass.LOGGER.warn("Row " + rowIndex + " too short (" + row.length() + " chars), padded to: '" + paddedRow + "'");
            return validateCharacters(paddedRow, rowIndex);
        } else {
            // Truncate
            String truncatedRow = row.substring(0, GUI_COLS);
            CobblePass.LOGGER.warn("Row " + rowIndex + " too long (" + row.length() + " chars), truncated to: '" + truncatedRow + "'");
            return validateCharacters(truncatedRow, rowIndex);
        }
    }

    /**
     * Validates that all characters in a row are valid placeholder characters.
     *
     * @param row The row to validate
     * @param rowIndex The row index for error reporting
     * @return The validated row (may have invalid characters replaced)
     */
    private static String validateCharacters(String row, int rowIndex) {
        StringBuilder validatedRow = new StringBuilder();
        boolean hasInvalidChars = false;

        for (int i = 0; i < row.length(); i++) {
            char c = row.charAt(i);
            if (isValidPlaceholderCharacter(c)) {
                validatedRow.append(c);
            } else {
                validatedRow.append(' '); // Replace invalid characters with space
                hasInvalidChars = true;
            }
        }

        if (hasInvalidChars) {
            CobblePass.LOGGER.warn("Row " + rowIndex + " contained invalid characters, replaced with spaces: '" + validatedRow + "'");
        }

        return validatedRow.toString();
    }
    
    /**
     * Checks if a character is a valid placeholder character.
     * 
     * @param c The character to check
     * @return true if the character is valid
     */
    private static boolean isValidPlaceholderCharacter(char c) {
        // Valid characters include letters, numbers, spaces (empty slots), and common symbols used in GUI layouts
        return Character.isLetterOrDigit(c) || 
               c == ' ' || c == '#' || c == '-' || c == '<' || c == '>' || 
               c == 'i' || c == 'B' || c == 'P' || c == 'L' || c == 'M' || 
               c == 'f' || c == 'r' || c == 's';
    }
    
    /**
     * Generates a default compact structure when parsing fails or no structure is provided.
     *
     * @return Default compact structure
     */
    private static List<String> generateDefaultCompactStructure() {
        return List.of(
            "#i #B# P#",  // Row 0: Border, XP Info, Space, Border, Progress, Border, Space, Premium Status, Border
            "         ",  // Row 1: All empty spaces
            "#L fffff #",  // Row 2: Border, Free Label, Space, Free Rewards (5), Space, Border
            "#M rrrrr #",  // Row 3: Border, Premium Label, Space, Premium Rewards (5), Space, Border
            "         ",  // Row 4: All empty spaces
            "<   #   >"   // Row 5: Previous, Space, Space, Space, Border, Space, Space, Space, Next
        );
    }
    
    /**
     * Validates the overall structure integrity.
     * Checks for required placeholder types and proper positioning.
     * 
     * @param structure The structure to validate
     * @return ValidationResult containing validation status and messages
     */
    public static ValidationResult validateStructure(List<String> structure) {
        ValidationResult result = new ValidationResult();
        
        if (structure == null || structure.size() != GUI_ROWS) {
            result.addError("Structure must have exactly " + GUI_ROWS + " rows");
            return result;
        }
        
        // Check each row length
        for (int i = 0; i < structure.size(); i++) {
            String row = structure.get(i);
            if (row == null || row.length() != GUI_COLS) {
                result.addError("Row " + i + " must be exactly " + GUI_COLS + " characters long");
            }
        }
        
        // Check for required placeholder types
        boolean hasRewardSlots = false;
        boolean hasNavigation = false;
        
        for (String row : structure) {
            if (row.contains("f") || row.contains("r")) {
                hasRewardSlots = true;
            }
            if (row.contains("<") || row.contains(">")) {
                hasNavigation = true;
            }
        }
        
        if (!hasRewardSlots) {
            result.addWarning("No reward slots (f or r) found in structure");
        }
        
        if (!hasNavigation) {
            result.addWarning("No navigation elements (< or >) found in structure");
        }
        
        return result;
    }
    
    /**
     * Result of structure validation containing errors and warnings.
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
    }
}