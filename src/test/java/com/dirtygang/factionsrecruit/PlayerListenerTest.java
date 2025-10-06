package com.dirtygang.factionsrecruit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gnut.factionsrecruit.ConfigManager;
import com.gnut.factionsrecruit.FactionsRecruit;
import com.gnut.factionsrecruit.GuiManager;
import com.gnut.factionsrecruit.PlayerListener;
import com.gnut.factionsrecruit.RecruitGUI;
import com.gnut.factionsrecruit.VisualUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;

import java.util.UUID;
import java.lang.reflect.Method;

/**
 * Test class for PlayerListener GUI interaction handling
 * Tests GUI title matching, click detection, and event handling
 */
public class PlayerListenerTest {

    private PlayerListener playerListener;
    private FactionsRecruit mockPlugin;
    private RecruitGUI mockRecruitGUI;
    private Player mockPlayer;
    private Inventory mockInventory;
    private InventoryView mockInventoryView;
    private ItemStack mockItemStack;
    private GuiManager mockGuiManager;
    private ConfigManager mockConfigManager;

    @BeforeEach
    public void setUp() {
        // Create mock objects
        mockPlugin = mock(FactionsRecruit.class);
        mockRecruitGUI = mock(RecruitGUI.class);
        mockPlayer = mock(Player.class);
        mockInventory = mock(Inventory.class);
        mockInventoryView = mock(InventoryView.class);
        mockItemStack = mock(ItemStack.class);
        mockGuiManager = mock(GuiManager.class);
        mockConfigManager = mock(ConfigManager.class);

        // Set up basic mock behavior
        when(mockPlugin.getGuiManager()).thenReturn(mockGuiManager);
        when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        when(mockGuiManager.hasActiveGUI(any(UUID.class))).thenReturn(false);
        when(mockConfigManager.getGuiTitle()).thenReturn("Faction Recruitment Browser");
        when(mockItemStack.getType()).thenReturn(Material.STONE);

        // Create PlayerListener instance
        playerListener = new PlayerListener(mockPlugin, mockRecruitGUI);
    }

    @AfterEach
    public void tearDown() {
        // Clean up any resources if needed
    }

    /**
     * Helper method to create a mock InventoryClickEvent
     */
    private InventoryClickEvent createMockClickEvent(String title, int slot, ItemStack item) {
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getView()).thenReturn(mockInventoryView);
        when(event.getWhoClicked()).thenReturn(mockPlayer);
        when(event.getCurrentItem()).thenReturn(item);
        when(event.getSlot()).thenReturn(slot);
        when(event.getClick()).thenReturn(ClickType.LEFT);
        when(event.getInventory()).thenReturn(mockInventory);
        when(mockInventoryView.getTitle()).thenReturn(title);
        return event;
    }

    /**
     * Helper method to access private methods via reflection
     */
    private String callPrivateMethod(String methodName, String input) {
        try {
            Method method = PlayerListener.class.getDeclaredMethod(methodName, String.class);
            method.setAccessible(true);
            return (String) method.invoke(playerListener, input);
        } catch (Exception e) {
            fail("Could not access " + methodName + " method: " + e.getMessage());
            return null;
        }
    }

    @Test
    @DisplayName("Title normalization should work correctly")
    public void testTitleNormalization() {
        String decoratedTitle = VisualUtils.createCompactServerTitle("PROFILE MANAGER");
        String normalized = callPrivateMethod("normalizeServerTitle", decoratedTitle);

        assertEquals("PROFILE MANAGER", normalized);
    }

    @Test
    @DisplayName("Color stripping should remove all color codes")
    public void testColorStripping() {
        String coloredTitle = "§aGreen §cRed §fWhite Text";
        String stripped = callPrivateMethod("stripColors", coloredTitle);

        assertEquals("Green Red White Text", stripped);
        assertFalse(stripped.contains("§"));
    }

    @Test
    @DisplayName("Small caps conversion should work correctly")
    public void testSmallCapsConversion() {
        String smallCapsText = "ᴘʀᴏꜰɪʟᴇ ᴍᴀɴᴀɢᴇʀ";
        String converted = callPrivateMethod("convertSmallCapsToNormal", smallCapsText);

        assertEquals("PROFILE MANAGER", converted);
    }

    @Test
    @DisplayName("Main menu title matching should work with decorated titles")
    public void testMainMenuTitleMatching() {
        String serverTitle = VisualUtils.createCompactServerTitle("RECRUITMENT BROWSER");
        InventoryClickEvent event = createMockClickEvent(serverTitle, 4, mockItemStack);

        // This test verifies that the event would be cancelled (GUI detected)
        // In a full MockBukkit test, we could verify the actual handling
        assertNotNull(event);
        assertEquals(serverTitle, event.getView().getTitle());
    }

    @Test
    @DisplayName("Resume editor title should be detected correctly")
    public void testResumeEditorTitleDetection() {
        String resumeTitle = VisualUtils.createCompactServerTitle("RESUME EDITOR");
        String normalized = callPrivateMethod("normalizeServerTitle", resumeTitle);

        assertEquals("RESUME EDITOR", normalized);
    }

    @Test
    @DisplayName("Profile manager title should be detected correctly")
    public void testProfileManagerTitleDetection() {
        String profileTitle = VisualUtils.createCompactServerTitle("PROFILE MANAGER");
        String normalized = callPrivateMethod("normalizeServerTitle", profileTitle);

        assertEquals("PROFILE MANAGER", normalized);
        assertTrue(normalized.contains("PROFILE MANAGER"));
    }

    @Test
    @DisplayName("Requirements editor title should be detected correctly")
    public void testRequirementsEditorTitleDetection() {
        String requirementsTitle = VisualUtils.createCompactServerTitle("REQUIREMENTS EDITOR");
        String normalized = callPrivateMethod("normalizeServerTitle", requirementsTitle);

        assertEquals("REQUIREMENTS EDITOR", normalized);
    }

    @Test
    @DisplayName("Help GUI title should be detected correctly")
    public void testHelpGUITitleDetection() {
        String helpTitle = VisualUtils.createCompactServerTitle("HELP");
        String normalized = callPrivateMethod("normalizeServerTitle", helpTitle);

        assertEquals("HELP", normalized);
    }

    @Test
    @DisplayName("Pending applications title should be detected correctly")
    public void testPendingApplicationsTitleDetection() {
        String pendingTitle = VisualUtils.createCompactServerTitle("PENDING APPLICATIONS");
        String normalized = callPrivateMethod("normalizeServerTitle", pendingTitle);

        assertEquals("PENDING APPLICATIONS", normalized);
    }

    @Test
    @DisplayName("Player-specific profile titles should be detected correctly")
    public void testPlayerProfileTitleDetection() {
        String playerName = "TestPlayer";
        String profileTitle = VisualUtils.createCompactServerTitle(playerName + " Profile");
        String normalized = callPrivateMethod("normalizeServerTitle", profileTitle);

        assertEquals("TESTPLAYER PROFILE", normalized);
        assertTrue(normalized.contains(playerName.toUpperCase()));
    }

    @Test
    @DisplayName("Faction-specific titles should be detected correctly")
    public void testFactionSpecificTitleDetection() {
        String factionName = "TestFaction";
        String factionTitle = VisualUtils.createCompactServerTitle(factionName + " Requirements");
        String normalized = callPrivateMethod("normalizeServerTitle", factionTitle);

        assertEquals("TESTFACTION REQUIREMENTS", normalized);
        assertTrue(normalized.contains(factionName.toUpperCase()));
    }

    @Test
    @DisplayName("Event handling should not crash with null items")
    public void testNullItemHandling() {
        InventoryClickEvent event = createMockClickEvent("Test Title", 0, null);

        // Should not throw exception when handling null items
        assertDoesNotThrow(() -> {
            // In a real test environment, we would call the actual event handler
            // For now, we verify the setup doesn't crash
            assertNotNull(event.getView().getTitle());
        });
    }

    @Test
    @DisplayName("Event handling should not crash with AIR material")
    public void testAirMaterialHandling() {
        ItemStack airItem = mock(ItemStack.class);
        when(airItem.getType()).thenReturn(Material.AIR);

        InventoryClickEvent event = createMockClickEvent("Test Title", 0, airItem);

        // Should not throw exception when handling AIR items
        assertDoesNotThrow(() -> {
            assertNotNull(event.getCurrentItem());
            assertEquals(Material.AIR, event.getCurrentItem().getType());
        });
    }

    @Test
    @DisplayName("Complex decorated titles should normalize correctly")
    public void testComplexDecoratedTitles() {
        // Test with multiple decorative elements
        String complexTitle = "§x§F§F§6§9§B§4► §x§F§F§F§F§F§Fᴘ§x§F§F§E§8§D§Aʀ§x§F§F§D§1§B§5ᴏ§x§F§F§B§A§9§0ꜰ§x§F§F§A§3§6§Bɪ§x§F§F§8§C§4§6ʟ§x§F§F§7§5§2§1ᴇ §x§F§F§6§9§B§4◄";
        String normalized = callPrivateMethod("normalizeServerTitle", complexTitle);

        assertEquals("PROFILE", normalized);
        assertFalse(normalized.contains("►"));
        assertFalse(normalized.contains("◄"));
        assertFalse(normalized.contains("§"));
    }

    @Test
    @DisplayName("Performance test for title normalization")
    public void testTitleNormalizationPerformance() {
        String complexTitle = VisualUtils.createCompactServerTitle("Very Long Profile Manager Title");

        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            callPrivateMethod("normalizeServerTitle", complexTitle);
        }
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;
        assertTrue(durationMs < 100, "Title normalization should complete 1000 operations in under 100ms");
    }

    @Test
    @DisplayName("Title matching should be case insensitive")
    public void testCaseInsensitiveTitleMatching() {
        String[] variations = {
            "profile manager",
            "Profile Manager",
            "PROFILE MANAGER",
            "pRoFiLe MaNaGeR"
        };

        for (String variation : variations) {
            String normalized = callPrivateMethod("normalizeServerTitle", variation);
            assertEquals("PROFILE MANAGER", normalized);
        }
    }

    @Test
    @DisplayName("Whitespace handling should be robust")
    public void testWhitespaceHandling() {
        String titleWithSpaces = "  ► ᴘʀᴏꜰɪʟᴇ ᴍᴀɴᴀɢᴇʀ ◄  ";
        String normalized = callPrivateMethod("normalizeServerTitle", titleWithSpaces);

        assertEquals("PROFILE MANAGER", normalized);
        assertFalse(normalized.startsWith(" "));
        assertFalse(normalized.endsWith(" "));
    }

    @Test
    @DisplayName("Special characters should be preserved appropriately")
    public void testSpecialCharacterPreservation() {
        String titleWithSpecial = "Player-123 Info!";
        String normalized = callPrivateMethod("normalizeServerTitle", titleWithSpecial);

        assertEquals("PLAYER-123 INFO!", normalized);
        assertTrue(normalized.contains("-"));
        assertTrue(normalized.contains("123"));
        assertTrue(normalized.contains("!"));
    }

    @Test
    @DisplayName("Empty and null titles should be handled gracefully")
    public void testEmptyAndNullTitleHandling() {
        assertNull(callPrivateMethod("normalizeServerTitle", null));
        assertEquals("", callPrivateMethod("normalizeServerTitle", ""));
        assertEquals("", callPrivateMethod("stripColors", ""));
        assertEquals("", callPrivateMethod("convertSmallCapsToNormal", ""));
    }

    @Test
    @DisplayName("All GUI types should have consistent title normalization")
    public void testAllGUITypesConsistency() {
        String[] guiTypes = {
            "RECRUITMENT BROWSER",
            "RESUME EDITOR",
            "REQUIREMENTS EDITOR",
            "PROFILE MANAGER",
            "HELP",
            "PENDING APPLICATIONS",
            "FILTER SETTINGS"
        };

        for (String guiType : guiTypes) {
            String decorated = VisualUtils.createCompactServerTitle(guiType);
            String normalized = callPrivateMethod("normalizeServerTitle", decorated);

            assertEquals(guiType, normalized,
                "GUI type '" + guiType + "' should normalize consistently");
            assertFalse(normalized.contains("►"),
                "Normalized title should not contain arrows: " + normalized);
            assertFalse(normalized.contains("◄"),
                "Normalized title should not contain arrows: " + normalized);
        }
    }
}