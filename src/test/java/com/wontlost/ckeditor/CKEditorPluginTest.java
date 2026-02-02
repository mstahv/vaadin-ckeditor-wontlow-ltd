package com.wontlost.ckeditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CKEditorPlugin enum.
 */
class CKEditorPluginTest {

    @Test
    @DisplayName("All plugins should have non-null JS names")
    void allPluginsShouldHaveJsNames() {
        for (CKEditorPlugin plugin : CKEditorPlugin.values()) {
            assertThat(plugin.getJsName())
                .as("Plugin %s should have a JS name", plugin.name())
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("All plugins should have a category")
    void allPluginsShouldHaveCategory() {
        for (CKEditorPlugin plugin : CKEditorPlugin.values()) {
            assertThat(plugin.getCategory())
                .as("Plugin %s should have a category", plugin.name())
                .isNotNull();
        }
    }

    @Test
    @DisplayName("Core plugins should exist")
    void corePluginsShouldExist() {
        assertThat(CKEditorPlugin.ESSENTIALS).isNotNull();
        assertThat(CKEditorPlugin.PARAGRAPH).isNotNull();
        assertThat(CKEditorPlugin.UNDO).isNotNull();
    }

    @Test
    @DisplayName("Basic style plugins should have correct toolbar items")
    void basicStylePluginsShouldHaveToolbarItems() {
        assertThat(CKEditorPlugin.BOLD.getToolbarItems()).contains("bold");
        assertThat(CKEditorPlugin.ITALIC.getToolbarItems()).contains("italic");
        assertThat(CKEditorPlugin.UNDERLINE.getToolbarItems()).contains("underline");
    }

    @Test
    @DisplayName("All built-in plugins should not be marked as premium")
    void allBuiltInPluginsShouldNotBePremium() {
        // All built-in plugins are free - premium features require
        // the ckeditor5-premium-features package and CustomPlugin
        for (CKEditorPlugin plugin : CKEditorPlugin.values()) {
            assertThat(plugin.isPremium())
                .as("Plugin %s should not be premium", plugin.name())
                .isFalse();
        }
    }

    @Test
    @DisplayName("getByCategory should return correct plugins")
    void getByCategoryShouldReturnCorrectPlugins() {
        Set<CKEditorPlugin> basicStyles = CKEditorPlugin.getByCategory(CKEditorPlugin.Category.BASIC_STYLES);

        assertThat(basicStyles)
            .contains(CKEditorPlugin.BOLD, CKEditorPlugin.ITALIC, CKEditorPlugin.UNDERLINE);
    }

    @Test
    @DisplayName("fromJsName should find plugin by JS name")
    void fromJsNameShouldFindPlugin() {
        assertThat(CKEditorPlugin.fromJsName("Bold")).isEqualTo(CKEditorPlugin.BOLD);
        assertThat(CKEditorPlugin.fromJsName("Table")).isEqualTo(CKEditorPlugin.TABLE);
        assertThat(CKEditorPlugin.fromJsName("NonExistent")).isNull();
    }

    @Test
    @DisplayName("List plugin should have multiple toolbar items")
    void listPluginShouldHaveMultipleToolbarItems() {
        Set<String> toolbarItems = CKEditorPlugin.LIST.getToolbarItems();

        assertThat(toolbarItems)
            .hasSize(2)
            .contains("bulletedList", "numberedList");
    }

    @Test
    @DisplayName("Category enum should have display names")
    void categoryShouldHaveDisplayNames() {
        assertThat(CKEditorPlugin.Category.CORE.getDisplayName()).isEqualTo("Core");
        assertThat(CKEditorPlugin.Category.BASIC_STYLES.getDisplayName()).isEqualTo("Basic Styles");
        assertThat(CKEditorPlugin.Category.CUSTOM.getDisplayName()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("STYLE plugin should have style toolbar item")
    void stylePluginShouldHaveStyleToolbarItem() {
        assertThat(CKEditorPlugin.STYLE.getToolbarItems()).contains("style");
        assertThat(CKEditorPlugin.STYLE.getCategory()).isEqualTo(CKEditorPlugin.Category.HTML);
        assertThat(CKEditorPlugin.STYLE.getJsName()).isEqualTo("Style");
    }
}
