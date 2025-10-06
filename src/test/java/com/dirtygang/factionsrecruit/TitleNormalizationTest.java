package com.dirtygang.factionsrecruit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.PlayerListener;
import com.gnut.factionsrecruit.interfaces.RecruitGUI;
import com.gnut.factionsrecruit.util.VisualUtils;

import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

/**
 * Test class for title normalization logic in PlayerListener
 * Tests the critical title matching system that enables GUI interactions
 */
public class TitleNormalizationTest {

    private PlayerListener playerListener;
    private FactionsRecruit mockPlugin;
    private RecruitGUI mockRecruitGUI;

    @BeforeEach
    public void setUp() {
        // Create minimal mock objects for testing
        mockPlugin = null; // Will be mocked properly when MockBukkit is working
        mockRecruitGUI = null;
        playerListener = new PlayerListener(mockPlugin, mockRecruitGUI);
    }

    /**
     * Helper method to access private normalizeServerTitle method via reflection
     */
    private String normalizeServerTitle(String text) {
        try {
            Method method = PlayerListener.class.getDeclaredMethod("normalizeServerTitle", String.class);
            method.setAccessible(true);
            return (String) method.invoke(playerListener, text);
        } catch (Exception e) {
            fail("Could not access normalizeServerTitle method: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to access private convertSmallCapsToNormal method via reflection
     */
    private String convertSmallCapsToNormal(String text) {
        try {
            Method method = PlayerListener.class.getDeclaredMethod("convertSmallCapsToNormal", String.class);
            method.setAccessible(true);
            return (String) method.invoke(playerListener, text);
        } catch (Exception e) {
            fail("Could not access convertSmallCapsToNormal method: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to access private stripColors method via reflection
     */
    private String stripColors(String text) {
        try {
            Method method = PlayerListener.class.getDeclaredMethod("stripColors", String.class);
            method.setAccessible(true);
            return (String) method.invoke(playerListener, text);
        } catch (Exception e) {
            fail("Could not access stripColors method: " + e.getMessage());
            return null;
        }
    }

    @Test
    @DisplayName("Small caps conversion should handle all uppercase letters")
    public void testSmallCapsToNormalConversion() {
        String smallCapsText = "ᴘʀᴏꜰɪʟᴇ ᴍᴀɴᴀɢᴇʀ";
        String result = convertSmallCapsToNormal(smallCapsText);

        assertEquals("PROFILE MANAGER", result);
    }

    @Test
    @DisplayName("Small caps conversion should handle mixed text")
    public void testSmallCapsConversionMixed() {
        String mixedText = "ᴘʟᴀʏᴇʀ 123 ɪɴꜰᴏ";
        String result = convertSmallCapsToNormal(mixedText);

        assertEquals("PLAYER 123 INFO", result);
    }

    @Test
    @DisplayName("Small caps conversion should handle null and empty strings")
    public void testSmallCapsConversionEdgeCases() {
        assertNull(convertSmallCapsToNormal(null));
        assertEquals("", convertSmallCapsToNormal(""));
        assertEquals(" ", convertSmallCapsToNormal(" "));
    }

    @Test
    @DisplayName("Server title normalization should remove decorative arrows")
    public void testServerTitleNormalizationArrows() {
        String decoratedTitle = "► ᴘʀᴏꜰɪʟᴇ ᴍᴀɴᴀɢᴇʀ ◄";
        String result = normalizeServerTitle(decoratedTitle);

        assertEquals("PROFILE MANAGER", result);
        assertFalse(result.contains("►"));
        assertFalse(result.contains("◄"));
    }

    @Test
    @DisplayName("Server title normalization should handle complex decorated titles")
    public void testServerTitleNormalizationComplex() {
        // Simulate what createCompactServerTitle produces
        String complexTitle = "§x§F§F§6§9§B§4► §x§F§F§F§F§F§Fᴘ§x§F§F§E§8§D§Aʀ§x§F§F§D§1§B§5ᴏ§x§F§F§B§A§9§0ꜰ§x§F§F§A§3§6§Bɪ§x§F§F§8§C§4§6ʟ§x§F§F§7§5§2§1ᴇ §x§F§F§6§9§B§4◄";
        String result = normalizeServerTitle(complexTitle);

        assertEquals("PROFILE", result);
    }

    @Test
    @DisplayName("Title normalization should handle null input")
    public void testTitleNormalizationNull() {
        assertNull(normalizeServerTitle(null));
    }

    @Test
    @DisplayName("Title normalization should handle empty string")
    public void testTitleNormalizationEmpty() {
        String result = normalizeServerTitle("");
        assertEquals("", result);
    }

    @Test
    @DisplayName("Title normalization should handle plain text")
    public void testTitleNormalizationPlainText() {
        String plainTitle = "Profile Manager";
        String result = normalizeServerTitle(plainTitle);

        assertEquals("PROFILE MANAGER", result);
    }

    @Test
    @DisplayName("Title normalization should handle whitespace correctly")
    public void testTitleNormalizationWhitespace() {
        String titleWithSpaces = "  ► ᴘʀᴏꜰɪʟᴇ ᴍᴀɴᴀɢᴇʀ ◄  ";
        String result = normalizeServerTitle(titleWithSpaces);

        assertEquals("PROFILE MANAGER", result);
    }

    @Test
    @DisplayName("Color stripping should remove Minecraft color codes")
    public void testColorStripping() {
        String coloredText = "§aGreen §cRed §fWhite";
        String result = stripColors(coloredText);

        assertEquals("Green Red White", result);
        assertFalse(result.contains("§"));
    }

    @Test
    @DisplayName("Color stripping should handle gradient color codes")
    public void testGradientColorStripping() {
        String gradientText = "§x§F§F§6§9§B§4Text";
        String result = stripColors(gradientText);

        assertEquals("Text", result);
        assertFalse(result.contains("§"));
    }

    @Test
    @DisplayName("Title matching pipeline should work end-to-end")
    public void testTitleMatchingPipeline() {
        // Test the complete pipeline: decorated server title -> normalized
        String originalTitle = "PROFILE MANAGER";

        // Step 1: Convert to server format (simulating VisualUtils.createCompactServerTitle)
        String serverTitle = VisualUtils.createCompactServerTitle(originalTitle);

        // Step 2: Normalize back for matching
        String normalized = normalizeServerTitle(serverTitle);

        assertEquals("PROFILE MANAGER", normalized);
    }

    @Test
    @DisplayName("Common GUI titles should normalize correctly")
    public void testCommonGUITitles() {
        // Test all the GUI titles that are checked in PlayerListener
        String[] expectedTitles = {
            "RECRUITMENT BROWSER",
            "RESUME EDITOR",
            "REQUIREMENTS EDITOR",
            "PROFILE MANAGER",
            "HELP",
            "PENDING APPLICATIONS"
        };

        for (String title : expectedTitles) {
            String serverFormatted = VisualUtils.createCompactServerTitle(title);
            String normalized = normalizeServerTitle(serverFormatted);

            assertEquals(title, normalized,
                "Title '" + title + "' should normalize correctly from server format");
        }
    }

    @Test
    @DisplayName("Player-specific titles should normalize correctly")
    public void testPlayerSpecificTitles() {
        String playerName = "TestPlayer";
        String profileTitle = playerName + " Profile";

        String serverFormatted = VisualUtils.createCompactServerTitle(profileTitle);
        String normalized = normalizeServerTitle(serverFormatted);

        assertEquals("TESTPLAYER PROFILE", normalized);
    }

    @Test
    @DisplayName("Faction-specific titles should normalize correctly")
    public void testFactionSpecificTitles() {
        String factionName = "TestFaction";
        String factionTitle = factionName + " Requirements";

        String normalized = normalizeServerTitle(factionTitle);

        assertEquals("TESTFACTION REQUIREMENTS", normalized);
    }

    @Test
    @DisplayName("Case insensitive matching should work")
    public void testCaseInsensitiveMatching() {
        String[] variations = {
            "profile manager",
            "Profile Manager",
            "PROFILE MANAGER",
            "pRoFiLe MaNaGeR"
        };

        for (String variation : variations) {
            String normalized = normalizeServerTitle(variation);
            assertEquals("PROFILE MANAGER", normalized);
        }
    }

    @Test
    @DisplayName("Special characters should be preserved appropriately")
    public void testSpecialCharacterHandling() {
        String titleWithSpecial = "Player-123 Info!";
        String normalized = normalizeServerTitle(titleWithSpecial);

        assertEquals("PLAYER-123 INFO!", normalized);
    }

    @Test
    @DisplayName("Performance test - title normalization should be fast")
    public void testTitleNormalizationPerformance() {
        String complexTitle = VisualUtils.createCompactServerTitle("Very Long Profile Manager Title");

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            normalizeServerTitle(complexTitle);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 100, "Title normalization should complete 1000 operations in under 100ms");
    }
}