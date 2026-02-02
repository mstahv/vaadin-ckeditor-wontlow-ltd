package com.wontlost.ckeditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CKEditorTheme enum.
 */
class CKEditorThemeTest {

    @Test
    @DisplayName("All themes should have lowercase JS names")
    void allThemesShouldHaveLowercaseJsNames() {
        assertThat(CKEditorTheme.LIGHT.getJsName()).isEqualTo("light");
        assertThat(CKEditorTheme.DARK.getJsName()).isEqualTo("dark");
    }

    @Test
    @DisplayName("toString should return JS name")
    void toStringShouldReturnJsName() {
        assertThat(CKEditorTheme.LIGHT.toString()).isEqualTo("light");
        assertThat(CKEditorTheme.DARK.toString()).isEqualTo("dark");
    }

    @Test
    @DisplayName("Should have exactly 3 themes (AUTO, LIGHT, DARK)")
    void shouldHaveExactly3Themes() {
        assertThat(CKEditorTheme.values()).hasSize(3);
    }

    @Test
    @DisplayName("AUTO theme should have lowercase JS name")
    void autoThemeShouldHaveLowercaseJsName() {
        assertThat(CKEditorTheme.AUTO.getJsName()).isEqualTo("auto");
    }

    @Test
    @DisplayName("valueOf should work with enum names")
    void valueOfShouldWorkWithEnumNames() {
        assertThat(CKEditorTheme.valueOf("LIGHT")).isEqualTo(CKEditorTheme.LIGHT);
        assertThat(CKEditorTheme.valueOf("DARK")).isEqualTo(CKEditorTheme.DARK);
    }
}
