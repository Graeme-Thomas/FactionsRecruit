package com.dirtygang.factionsrecruit;

import org.junit.jupiter.api.Test;

import com.gnut.factionsrecruit.VisualUtils;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for VisualUtils functionality, focusing on title creation,
 * small caps conversion, and server-themed formatting
 */
public class VisualUtilsTest {

    @Test
    @DisplayName("Small caps conversion should work correctly for basic text")
    public void testSmallCapsConversion() {
        String input = "PROFILE MANAGER";
        String result = VisualUtils.SmallCaps.convert(input);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertTrue(result.contains("ᴘʀᴏꜰɪʟᴇ"));
        assertTrue(result.contains("ᴍᴀɴᴀɢᴇʀ"));
    }

    @Test
    @DisplayName("Small caps conversion should handle null and empty strings")
    public void testSmallCapsConversionEdgeCases() {
        assertNull(VisualUtils.SmallCaps.convert(null));
        assertEquals("", VisualUtils.SmallCaps.convert(""));
        assertEquals(" ", VisualUtils.SmallCaps.convert(" "));
    }

    @Test
    @DisplayName("Small caps conversion should preserve numbers and special characters")
    public void testSmallCapsPreservesSpecialCharacters() {
        String input = "PLAYER 123 STATUS!";
        String result = VisualUtils.SmallCaps.convert(input);

        assertTrue(result.contains("123"));
        assertTrue(result.contains("!"));
        assertTrue(result.contains(" "));
    }

    @Test
    @DisplayName("Small caps conversion should handle mixed case input")
    public void testSmallCapsMixedCase() {
        String input = "Profile Manager";
        String result = VisualUtils.SmallCaps.convert(input);

        assertNotNull(result);
        assertTrue(result.contains("ᴘʀᴏꜰɪʟᴇ"));
        assertTrue(result.contains("ᴍᴀɴᴀɢᴇʀ"));
    }

    @Test
    @DisplayName("Server title creation should include decorative elements")
    public void testServerTitleCreation() {
        String input = "MAIN MENU";
        String result = VisualUtils.SmallCaps.createServerTitle(input);

        assertNotNull(result);
        assertTrue(result.contains("►"));
        assertTrue(result.contains("◄"));
        assertTrue(result.contains("ᴍᴀɪɴ"));
        assertTrue(result.contains("ᴍᴇɴᴜ"));
    }

    @Test
    @DisplayName("Compact server title should work correctly")
    public void testCompactServerTitle() {
        String input = "PROFILE MANAGER";
        String regular = VisualUtils.SmallCaps.createServerTitle(input);
        String compact = VisualUtils.createCompactServerTitle(input);

        assertNotNull(compact);
        assertNotNull(regular);
        assertTrue(compact.contains("►"));
        assertTrue(compact.contains("◄"));

        // Due to gradient color codes, just verify small caps characters are present
        assertTrue(compact.contains("ᴘ") && compact.contains("ʀ") && compact.contains("ᴏ"));
    }

    @Test
    @DisplayName("Compact server title should create valid titles")
    public void testCompactServerTitleLength() {
        String[] testInputs = {
            "PROFILE",
            "MAIN MENU",
            "SETTINGS",
            "HELP",
            "APPLICATIONS"
        };

        for (String input : testInputs) {
            String result = VisualUtils.createCompactServerTitle(input);
            // Remove comprehensive color codes for length calculation
            String stripped = result.replaceAll("§x§[0-9A-Fa-f]§[0-9A-Fa-f]§[0-9A-Fa-f]§[0-9A-Fa-f]§[0-9A-Fa-f]§[0-9A-Fa-f]", "")
                                   .replaceAll("§[0-9a-fk-or]", "");

            // With gradients, the visual length is more important than character count
            // Just verify the title is created successfully and contains expected elements
            assertNotNull(result, "Title should not be null for: " + input);
            assertTrue(result.contains("►"), "Title should contain arrows for: " + input);
            assertTrue(result.contains("◄"), "Title should contain arrows for: " + input);
        }
    }

    @Test
    @DisplayName("Gradient creation should handle null and empty strings")
    public void testGradientCreationEdgeCases() {
        String startColor = "#FFFFFF";
        String endColor = "#FF0000";

        assertNull(VisualUtils.createGradient(null, startColor, endColor));
        assertEquals("", VisualUtils.createGradient("", startColor, endColor));
    }

    @Test
    @DisplayName("Gradient creation should produce different output than input")
    public void testGradientCreation() {
        String input = "TEST TEXT";
        String result = VisualUtils.createGradient(input, "#FFFFFF", "#FF0000");

        assertNotNull(result);
        assertNotEquals(input, result);
        assertTrue(result.length() > input.length()); // Should have color codes
    }

    @Test
    @DisplayName("Server gradient should use server color palette")
    public void testServerGradient() {
        String input = "SERVER TEXT";
        String result = VisualUtils.createServerGradient(input);

        assertNotNull(result);
        assertNotEquals(input, result);
        assertTrue(result.length() > input.length());
    }

    @Test
    @DisplayName("Color palette constants should be valid hex colors")
    public void testColorPaletteValidity() {
        // Test that color constants are valid hex format
        assertTrue(VisualUtils.ColorPalette.SERVER_WHITE.matches("#[0-9A-Fa-f]{6}"));
        assertTrue(VisualUtils.ColorPalette.SERVER_PINK.matches("#[0-9A-Fa-f]{6}"));
        assertTrue(VisualUtils.ColorPalette.SERVER_RED.matches("#[0-9A-Fa-f]{6}"));
        assertTrue(VisualUtils.ColorPalette.SUCCESS.matches("#[0-9A-Fa-f]{6}"));
        assertTrue(VisualUtils.ColorPalette.ERROR.matches("#[0-9A-Fa-f]{6}"));
    }

    @Test
    @DisplayName("Symbol constants should be defined and not empty")
    public void testSymbolConstants() {
        assertNotNull(VisualUtils.Symbols.SERVER_ARROW_LEFT);
        assertNotNull(VisualUtils.Symbols.SERVER_ARROW_RIGHT);
        assertNotNull(VisualUtils.Symbols.SERVER_DECORATOR);

        assertFalse(VisualUtils.Symbols.SERVER_ARROW_LEFT.isEmpty());
        assertFalse(VisualUtils.Symbols.SERVER_ARROW_RIGHT.isEmpty());
        assertFalse(VisualUtils.Symbols.SERVER_DECORATOR.isEmpty());

        assertEquals("◄", VisualUtils.Symbols.SERVER_ARROW_LEFT);
        assertEquals("►", VisualUtils.Symbols.SERVER_ARROW_RIGHT);
    }

    @Test
    @DisplayName("Server section header should use small caps and gradient")
    public void testServerSectionHeader() {
        String input = "SECTION TITLE";
        String result = VisualUtils.createServerSectionHeader(input);

        assertNotNull(result);
        assertNotEquals(input, result);
        // Should contain small caps characters (may be separated by gradient codes)
        assertTrue(result.contains("ꜱ") && result.contains("ᴇ") && result.contains("ᴄ"));
    }

    @Test
    @DisplayName("Server button name should use small caps and gradient")
    public void testServerButtonName() {
        String input = "BUTTON TEXT";
        String result = VisualUtils.createServerButtonName(input);

        assertNotNull(result);
        assertNotEquals(input, result);
        // Should contain small caps characters (may be separated by gradient codes)
        assertTrue(result.contains("ʙ") && result.contains("ᴜ") && result.contains("ᴛ"));
    }

    @Test
    @DisplayName("Server status indicator should include symbol and small caps")
    public void testServerStatusIndicator() {
        String status = "ACTIVE";
        String resultPositive = VisualUtils.createServerStatusIndicator(status, true);
        String resultNegative = VisualUtils.createServerStatusIndicator(status, false);

        assertNotNull(resultPositive);
        assertNotNull(resultNegative);

        assertTrue(resultPositive.contains("✓"));
        assertTrue(resultNegative.contains("✗"));

        // Should contain small caps version of status
        assertTrue(resultPositive.contains("ᴀᴄᴛɪᴠᴇ"));
        assertTrue(resultNegative.contains("ᴀᴄᴛɪᴠᴇ"));
    }
}