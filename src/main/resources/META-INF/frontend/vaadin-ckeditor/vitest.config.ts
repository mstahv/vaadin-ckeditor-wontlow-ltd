/**
 * Vitest Configuration for Vaadin CKEditor 5
 *
 * Run tests with: npm test
 * Run in watch mode: npm run test:watch
 * Run with coverage: npm run test:coverage
 */

import { defineConfig } from 'vitest/config';

export default defineConfig({
    test: {
        // Use jsdom for DOM testing
        environment: 'jsdom',

        // Global test utilities (describe, it, expect, etc.)
        globals: true,

        // Test file patterns
        include: ['**/*.test.ts'],

        // Exclude node_modules and build outputs
        exclude: ['node_modules', 'dist', 'build'],

        // Coverage configuration
        coverage: {
            provider: 'v8',
            reporter: ['text', 'json', 'html'],
            include: [
                'plugin-resolver.ts',
                'upload-adapter.ts',
                'theme-manager.ts',
                'fallback-renderer.ts',
            ],
            exclude: [
                '**/*.test.ts',
                '**/node_modules/**',
            ],
        },

        // Reporter configuration
        reporters: ['default'],

        // Timeout for async tests (5 seconds)
        testTimeout: 5000,
    },

    // Resolve aliases for testing
    resolve: {
        alias: {
            // Mock ckeditor5-premium-features for testing
            'ckeditor5-premium-features': './test-mocks/ckeditor5-premium-features.ts',
        },
    },
});
