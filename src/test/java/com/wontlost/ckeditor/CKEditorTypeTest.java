package com.wontlost.ckeditor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for CKEditorType enum.
 */
class CKEditorTypeTest {

    @Test
    @DisplayName("All types should have lowercase JS names")
    void allTypesShouldHaveLowercaseJsNames() {
        assertThat(CKEditorType.CLASSIC.getJsName()).isEqualTo("classic");
        assertThat(CKEditorType.BALLOON.getJsName()).isEqualTo("balloon");
        assertThat(CKEditorType.INLINE.getJsName()).isEqualTo("inline");
        assertThat(CKEditorType.DECOUPLED.getJsName()).isEqualTo("decoupled");
    }

    @Test
    @DisplayName("toString should return JS name")
    void toStringShouldReturnJsName() {
        assertThat(CKEditorType.CLASSIC.toString()).isEqualTo("classic");
        assertThat(CKEditorType.BALLOON.toString()).isEqualTo("balloon");
    }

    @Test
    @DisplayName("Should have exactly 4 editor types")
    void shouldHaveExactly4EditorTypes() {
        assertThat(CKEditorType.values()).hasSize(4);
    }
}
