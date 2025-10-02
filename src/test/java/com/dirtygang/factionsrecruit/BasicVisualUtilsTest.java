package com.dirtygang.factionsrecruit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test class for VisualUtils functionality without Bukkit dependencies
 * Tests core title creation and small caps conversion logic
 */
public class BasicVisualUtilsTest {

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

        // Test that titles contain the expected decorators
        assertTrue(compact.contains("►"), "Compact title should contain ►: " + compact);
        assertTrue(compact.contains("◄"), "Compact title should contain ◄: " + compact);

        // Test that compact title contains small caps conversion (individual characters due to gradient)
        assertTrue(compact.contains("ᴘ") && compact.contains("ʀ") && compact.contains("ᴏ") && compact.contains("ꜰ") && compact.contains("ɪ") && compact.contains("ʟ") && compact.contains("ᴇ"),
                  "Compact should contain small caps profile characters: " + compact);

        System.out.println("Regular: '" + regular + "' (" + regular.length() + " chars)");
        System.out.println("Compact: '" + compact + "' (" + compact.length() + " chars)");
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

    @Test
    @DisplayName("Performance test - small caps conversion should be fast")
    public void testSmallCapsPerformance() {
        String input = "VERY LONG PROFILE MANAGER TITLE WITH MANY WORDS";

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            VisualUtils.SmallCaps.convert(input);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 50, "Small caps conversion should complete 1000 operations in under 50ms");
    }

    @Test
    @DisplayName("All GUI title examples should convert consistently")
    public void testAllGUITitlesConsistency() {
        String[] guiTypes = {
            "RECRUITMENT BROWSER",
            "RESUME EDITOR",
            "REQUIREMENTS EDITOR",
            "PROFILE MANAGER",
            "HELP",
            "PENDING APPLICATIONS"
        };

        for (String guiType : guiTypes) {
            String smallCaps = VisualUtils.SmallCaps.convert(guiType);
            String compact = VisualUtils.createCompactServerTitle(guiType);

            assertNotNull(smallCaps, "Small caps conversion failed for: " + guiType);
            assertNotNull(compact, "Compact title creation failed for: " + guiType);
            assertNotEquals(guiType, smallCaps, "Small caps should differ from original: " + guiType);

            // Compact titles should contain arrows
            assertTrue(compact.contains("►"), "Compact title should contain arrow: " + guiType);
            assertTrue(compact.contains("◄"), "Compact title should contain arrow: " + guiType);
        }
    }
}