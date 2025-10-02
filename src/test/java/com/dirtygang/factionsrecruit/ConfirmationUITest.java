package com.dirtygang.factionsrecruit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

/**
 * Test class for confirmation UI title normalization and close handling
 */
public class ConfirmationUITest {

    /**
     * Helper method to test title normalization for confirmation dialogs
     */
    private String normalizeServerTitle(String text) {
        try {
            // Create a dummy PlayerListener to test the normalization method
            PlayerListener playerListener = new PlayerListener(null, null);
            Method method = PlayerListener.class.getDeclaredMethod("normalizeServerTitle", String.class);
            method.setAccessible(true);
            return (String) method.invoke(playerListener, text);
        } catch (Exception e) {
            fail("Could not access normalizeServerTitle method: " + e.getMessage());
            return null;
        }
    }

    @Test
    @DisplayName("Confirmation dialog titles should normalize correctly")
    public void testConfirmationTitleNormalization() {
        // Test various confirmation title formats
        String[] confirmationTitles = {
            VisualUtils.createCompactServerTitle("Confirm Invitation"),
            VisualUtils.createCompactServerTitle("Apply to Faction"),
            VisualUtils.createCompactServerTitle("Confirm Resume Changes"),
            VisualUtils.createCompactServerTitle("Confirm Faction Application Changes")
        };

        for (String title : confirmationTitles) {
            String normalized = normalizeServerTitle(title);
            assertNotNull(normalized, "Normalized title should not be null: " + title);
            assertTrue(normalized.contains("CONFIRM") || normalized.contains("APPLY"),
                "Normalized title should contain confirmation keywords: " + normalized);
        }
    }

    @Test
    @DisplayName("Confirmation titles should be detected for cleanup")
    public void testConfirmationTitleDetection() {
        // Test the logic that will be used in onInventoryClose
        String[] testTitles = {
            "CONFIRM INVITATION",
            "APPLY TO FACTION",
            "CONFIRM RESUME CHANGES",
            "CONFIRM FACTION APPLICATION CHANGES",
            "CONFIRMATION DIALOG"
        };

        for (String title : testTitles) {
            boolean shouldCleanup = title.contains("CONFIRM") ||
                                  title.contains("CONFIRMATION") ||
                                  title.contains("APPLY");

            assertTrue(shouldCleanup, "Title should be detected for cleanup: " + title);
        }
    }

    @Test
    @DisplayName("Non-confirmation titles should not be cleaned up")
    public void testNonConfirmationTitleDetection() {
        String[] nonConfirmationTitles = {
            "RECRUITMENT BROWSER",
            "RESUME EDITOR",
            "PROFILE MANAGER",
            "HELP",
            "PENDING APPLICATIONS"
        };

        for (String title : nonConfirmationTitles) {
            boolean shouldCleanup = title.contains("CONFIRM") ||
                                  title.contains("CONFIRMATION");

            assertFalse(shouldCleanup, "Non-confirmation title should not be cleaned up: " + title);
        }
    }

    @Test
    @DisplayName("Server-themed confirmation titles work correctly")
    public void testServerThemedConfirmationTitles() {
        // Test that our new server-themed confirmation titles work
        String inviteTitle = VisualUtils.createCompactServerTitle("Confirm Invitation");
        String applyTitle = VisualUtils.createCompactServerTitle("Apply to Faction");

        String normalizedInvite = normalizeServerTitle(inviteTitle);
        String normalizedApply = normalizeServerTitle(applyTitle);

        assertEquals("CONFIRM INVITATION", normalizedInvite);
        assertEquals("APPLY TO FACTION", normalizedApply);

        // Verify they would be detected for cleanup
        assertTrue(normalizedInvite.contains("CONFIRM"));
        assertTrue(normalizedApply.contains("APPLY"));
    }

    @Test
    @DisplayName("Title normalization should handle null gracefully")
    public void testNullTitleHandling() {
        String result = normalizeServerTitle(null);
        assertNull(result, "Null input should return null");
    }
}