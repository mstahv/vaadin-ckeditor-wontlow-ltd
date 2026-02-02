package com.wontlost.ckeditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CKEditorPreset enum.
 */
class CKEditorPresetTest {

    @Test
    @DisplayName("All presets should have display names")
    void allPresetsShouldHaveDisplayNames() {
        for (CKEditorPreset preset : CKEditorPreset.values()) {
            assertThat(preset.getDisplayName())
                .as("Preset %s should have a display name", preset.name())
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("All presets should have plugins")
    void allPresetsShouldHavePlugins() {
        for (CKEditorPreset preset : CKEditorPreset.values()) {
            assertThat(preset.getPlugins())
                .as("Preset %s should have plugins", preset.name())
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Test
    @DisplayName("All presets should include core plugins")
    void allPresetsShouldIncludeCorePlugins() {
        for (CKEditorPreset preset : CKEditorPreset.values()) {
            assertThat(preset.getPlugins())
                .as("Preset %s should include ESSENTIALS", preset.name())
                .contains(CKEditorPlugin.ESSENTIALS);

            assertThat(preset.getPlugins())
                .as("Preset %s should include PARAGRAPH", preset.name())
                .contains(CKEditorPlugin.PARAGRAPH);
        }
    }

    @Test
    @DisplayName("BASIC preset should have minimal plugins")
    void basicPresetShouldHaveMinimalPlugins() {
        Set<CKEditorPlugin> plugins = CKEditorPreset.BASIC.getPlugins();

        assertThat(plugins)
            .contains(
                CKEditorPlugin.ESSENTIALS,
                CKEditorPlugin.PARAGRAPH,
                CKEditorPlugin.BOLD,
                CKEditorPlugin.ITALIC,
                CKEditorPlugin.LINK
            );

        // Should not include advanced features
        assertThat(plugins)
            .doesNotContain(
                CKEditorPlugin.TABLE,
                CKEditorPlugin.CODE_BLOCK,
                CKEditorPlugin.FONT_SIZE
            );
    }

    @Test
    @DisplayName("STANDARD preset should have more plugins than BASIC")
    void standardPresetShouldHaveMorePluginsThanBasic() {
        assertThat(CKEditorPreset.STANDARD.getPlugins().size())
            .isGreaterThan(CKEditorPreset.BASIC.getPlugins().size());
    }

    @Test
    @DisplayName("FULL preset should have more plugins than BASIC and STANDARD")
    void fullPresetShouldHaveMorePluginsThanBasicPresets() {
        int fullSize = CKEditorPreset.FULL.getPlugins().size();

        // FULL preset is optimized for fast loading, so it may have fewer plugins than DOCUMENT
        assertThat(fullSize)
            .isGreaterThan(CKEditorPreset.STANDARD.getPlugins().size())
            .isGreaterThan(CKEditorPreset.BASIC.getPlugins().size());
    }

    @Test
    @DisplayName("EMPTY preset should only have core plugins")
    void emptyPresetShouldOnlyHaveCorePlugins() {
        Set<CKEditorPlugin> plugins = CKEditorPreset.EMPTY.getPlugins();

        assertThat(plugins)
            .hasSize(2)
            .containsExactlyInAnyOrder(CKEditorPlugin.ESSENTIALS, CKEditorPlugin.PARAGRAPH);
    }

    @Test
    @DisplayName("All presets should have default toolbar")
    void allPresetsShouldHaveDefaultToolbar() {
        for (CKEditorPreset preset : CKEditorPreset.values()) {
            assertThat(preset.getDefaultToolbar())
                .as("Preset %s should have a toolbar array", preset.name())
                .isNotNull();
        }
    }

    @Test
    @DisplayName("hasPlugin should correctly check plugin existence")
    void hasPluginShouldCorrectlyCheckPluginExistence() {
        assertThat(CKEditorPreset.STANDARD.hasPlugin(CKEditorPlugin.BOLD)).isTrue();
        assertThat(CKEditorPreset.STANDARD.hasPlugin(CKEditorPlugin.TABLE)).isTrue();
        assertThat(CKEditorPreset.BASIC.hasPlugin(CKEditorPlugin.TABLE)).isFalse();
    }

    @Test
    @DisplayName("getEstimatedSize should return reasonable values")
    void getEstimatedSizeShouldReturnReasonableValues() {
        assertThat(CKEditorPreset.EMPTY.getEstimatedSize()).isLessThan(CKEditorPreset.BASIC.getEstimatedSize());
        assertThat(CKEditorPreset.BASIC.getEstimatedSize()).isLessThan(CKEditorPreset.STANDARD.getEstimatedSize());
        assertThat(CKEditorPreset.STANDARD.getEstimatedSize()).isLessThan(CKEditorPreset.FULL.getEstimatedSize());
    }

    @Test
    @DisplayName("DOCUMENT preset should include document-specific plugins")
    void documentPresetShouldIncludeDocumentPlugins() {
        Set<CKEditorPlugin> plugins = CKEditorPreset.DOCUMENT.getPlugins();

        assertThat(plugins)
            .contains(
                CKEditorPlugin.TITLE,
                CKEditorPlugin.WORD_COUNT,
                CKEditorPlugin.PAGE_BREAK,
                CKEditorPlugin.AUTOSAVE
            );
    }
}
