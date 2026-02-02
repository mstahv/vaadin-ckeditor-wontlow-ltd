package com.wontlost.ckeditor;

import java.util.Locale;

/**
 * CKEditor theme types.
 *
 * <p>Supports three modes:</p>
 * <ul>
 *   <li>{@link #AUTO} - Automatically syncs with Vaadin's Lumo theme. When Vaadin switches
 *       between light/dark mode, CKEditor will follow. Also respects OS-level dark mode
 *       preference if Vaadin doesn't have an explicit theme set.</li>
 *   <li>{@link #LIGHT} - Forces light theme regardless of Vaadin/OS settings.</li>
 *   <li>{@link #DARK} - Forces dark theme regardless of Vaadin/OS settings.</li>
 * </ul>
 */
public enum CKEditorTheme {

    /**
     * Auto theme - syncs with Vaadin's Lumo theme and OS dark mode preference.
     * This is the recommended default for seamless Vaadin integration.
     */
    AUTO,

    /**
     * Light theme - forces light mode regardless of Vaadin/OS settings.
     */
    LIGHT,

    /**
     * Dark theme - forces dark mode regardless of Vaadin/OS settings.
     */
    DARK;

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
