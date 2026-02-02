package com.wontlost.ckeditor;

import java.util.Locale;

/**
 * CKEditor editor types
 */
public enum CKEditorType {

    /**
     * Classic editor with fixed toolbar
     */
    CLASSIC,

    /**
     * Balloon editor with floating toolbar on selection
     */
    BALLOON,

    /**
     * Inline editor for direct page editing
     */
    INLINE,

    /**
     * Decoupled editor with separated toolbar
     */
    DECOUPLED;

    /**
     * Get JavaScript name
     */
    public String getJsName() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return getJsName();
    }
}
