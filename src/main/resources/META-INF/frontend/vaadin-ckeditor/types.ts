/**
 * Type Definitions for Vaadin CKEditor 5
 *
 * This file exports all public types for external use.
 * Import these types when building custom plugins or extending the editor.
 *
 * @packageDocumentation
 */

// Re-export types from plugin-resolver
export type {
    PluginConfig,
    FilterResult,
    FilterOptions,
} from './plugin-resolver';

// Re-export types from upload-adapter
export type {
    UploadResolver,
    UploadServer,
    FileLoader,
    UploadAdapter,
} from './upload-adapter';

// Re-export types from fallback-renderer
export type { FallbackMode } from './fallback-renderer';

// Re-export functions from plugin-resolver
export {
    registerCKEditorPlugin,
    getGlobalPluginRegistry,
    filterConflictingPlugins,
    PLUGIN_REGISTRY,
    PluginResolver,
} from './plugin-resolver';

// Re-export classes from modules
export { UploadAdapterManager } from './upload-adapter';
export { ThemeManager } from './theme-manager';
export { FallbackRenderer } from './fallback-renderer';

/**
 * Editor type options for VaadinCKEditor.
 */
export type EditorType = 'classic' | 'balloon' | 'inline' | 'decoupled';

/**
 * Theme type options for VaadinCKEditor.
 */
export type ThemeType = 'auto' | 'light' | 'dark';

/**
 * Toolbar style configuration for customizing CKEditor toolbar appearance.
 */
export interface ToolbarStyleConfig {
    /** Toolbar background color */
    background?: string;
    /** Toolbar border color */
    borderColor?: string;
    /** Toolbar border radius */
    borderRadius?: string;
    /** Default button background color */
    buttonBackground?: string;
    /** Button hover background color */
    buttonHoverBackground?: string;
    /** Button active/pressed background color */
    buttonActiveBackground?: string;
    /** Button "on" state background color (for toggles) */
    buttonOnBackground?: string;
    /** Button "on" state text color */
    buttonOnColor?: string;
    /** Default icon color */
    iconColor?: string;
    /** Per-button style overrides */
    buttonStyles?: Record<string, ButtonStyleConfig>;
}

/**
 * Individual button style configuration.
 */
export interface ButtonStyleConfig {
    /** Button background color */
    background?: string;
    /** Button hover background color */
    hoverBackground?: string;
    /** Button active/pressed background color */
    activeBackground?: string;
    /** Button icon color */
    iconColor?: string;
}

/**
 * Server communication interface for VaadinCKEditor.
 * Implement this interface to handle server-side communication.
 */
export interface VaadinServer {
    /** Send editor data to server */
    setEditorData(data: string): void;
    /** Trigger autosave */
    saveEditorData(data: string): void;
    /** Fire editor ready event */
    fireEditorReady(initTimeMs: number): void;
    /** Fire editor error event */
    fireEditorError(
        code: string,
        message: string,
        severity: string,
        recoverable: boolean,
        stackTrace: string
    ): void;
    /** Fire content change event */
    fireContentChange(oldContent: string, newContent: string, source: string): void;
    /** Fire fallback mode event */
    fireFallback(mode: string, reason: string, originalError: string): void;
    /** Handle file upload */
    handleFileUpload(
        uploadId: string,
        fileName: string,
        mimeType: string,
        base64Data: string
    ): void;
}

/**
 * Content change source types.
 */
export type ChangeSource =
    | 'API'
    | 'USER_INPUT'
    | 'UNDO_REDO'
    | 'PASTE'
    | 'COLLABORATION'
    | 'UNKNOWN';

/**
 * Error severity levels.
 */
export type ErrorSeverity = 'INFO' | 'WARNING' | 'ERROR' | 'FATAL';

/**
 * Debug mode flag.
 * Set `window.VAADIN_CKEDITOR_DEBUG = true` to enable debug logging.
 */
declare global {
    interface Window {
        VAADIN_CKEDITOR_DEBUG?: boolean;
    }
}
