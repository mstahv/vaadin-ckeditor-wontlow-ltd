package com.wontlost.ckeditor;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Custom plugin definition.
 * Supports third-party or user-defined CKEditor plugins.
 *
 * <p>Usage example:</p>
 * <pre>
 * // Define custom plugin
 * CustomPlugin myPlugin = CustomPlugin.builder("MyCustomPlugin")
 *     .withToolbarItems("myButton")
 *     .withImportPath("my-ckeditor-plugin")
 *     .build();
 *
 * // Use custom plugin
 * VaadinCKEditor.create()
 *     .withPreset(CKEditorPreset.STANDARD)
 *     .addCustomPlugin(myPlugin)
 *     .build();
 * </pre>
 *
 * <p>Custom plugins must meet CKEditor 5 plugin requirements:</p>
 * <ul>
 *   <li>Must be a class extending Plugin</li>
 *   <li>Must be provided via npm package or local module</li>
 *   <li>Export name must match jsName</li>
 * </ul>
 */
public class CustomPlugin {

    /**
     * 合法的 npm 包名正则表达式
     * 支持格式:
     * - 普通包名: my-package, ckeditor5-plugin
     * - 作用域包: @scope/package, @company/my-plugin
     * - npm 子路径导入: lodash/merge, @scope/package/subpath
     * - 相对路径: ./my-local-plugin, ../plugins/my-plugin, ../../shared/plugin
     * - 带扩展名: ./plugin.js, ../utils/helper.ts
     *
     * 禁止格式:
     * - 绝对路径: /etc/passwd, C:\Windows
     * - URL: http://evil.com/malware.js, file:///etc/passwd
     * - 深度路径遍历 (>2级): ../../../etc/passwd
     */
    private static final Pattern VALID_IMPORT_PATH = Pattern.compile(
        "^(?:" +
            // npm 作用域包 (支持子路径): @scope/package-name 或 @scope/package/subpath
            "@[a-z0-9][a-z0-9._-]*/[a-z0-9][a-z0-9._/-]*" +
            "|" +
            // 普通 npm 包名 (支持子路径): package-name 或 package/subpath
            "[a-z0-9][a-z0-9._/-]*" +
            "|" +
            // 相对路径: ./ 或 ../ 或 ../../ (最多向上 2 级)，支持扩展名
            "(?:\\.{1,2}/){1,2}(?:[a-zA-Z0-9_.-]+/)*[a-zA-Z0-9_.-]+" +
        ")$",
        Pattern.CASE_INSENSITIVE
    );

    private final String jsName;
    private final String importPath;
    private final Set<String> toolbarItems;
    private final Set<String> dependencies;
    private final boolean isPremium;

    private CustomPlugin(Builder builder) {
        this.jsName = Objects.requireNonNull(builder.jsName, "Plugin name cannot be null");
        this.importPath = builder.importPath;
        this.toolbarItems = builder.toolbarItems != null
            ? Collections.unmodifiableSet(builder.toolbarItems)
            : Collections.emptySet();
        this.dependencies = builder.dependencies != null
            ? Collections.unmodifiableSet(builder.dependencies)
            : Collections.emptySet();
        this.isPremium = builder.isPremium;
    }

    /**
     * Get JavaScript plugin name
     */
    public String getJsName() {
        return jsName;
    }

    /**
     * Get import path.
     * If null, import from ckeditor5 main package.
     */
    public String getImportPath() {
        return importPath;
    }

    /**
     * Get toolbar items provided by this plugin
     */
    public Set<String> getToolbarItems() {
        return toolbarItems;
    }

    /**
     * Get dependency plugin names
     */
    public Set<String> getDependencies() {
        return dependencies;
    }

    /**
     * Check if premium feature (import from ckeditor5-premium-features)
     */
    public boolean isPremium() {
        return isPremium;
    }

    /**
     * Create builder
     */
    public static Builder builder(String jsName) {
        return new Builder(jsName);
    }

    /**
     * Quick create custom plugin with import path
     */
    public static CustomPlugin of(String jsName, String importPath) {
        return builder(jsName).withImportPath(importPath).build();
    }

    /**
     * Quick create plugin from ckeditor5 main package
     */
    public static CustomPlugin fromCKEditor5(String jsName) {
        return builder(jsName).build();
    }

    /**
     * Quick create plugin from ckeditor5-premium-features
     */
    public static CustomPlugin fromPremium(String jsName) {
        return builder(jsName).premium().build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomPlugin that = (CustomPlugin) o;
        return jsName.equals(that.jsName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jsName);
    }

    @Override
    public String toString() {
        return "CustomPlugin{" +
            "jsName='" + jsName + '\'' +
            ", importPath='" + importPath + '\'' +
            ", isPremium=" + isPremium +
            '}';
    }

    /**
     * Custom plugin builder
     */
    public static class Builder {
        private final String jsName;
        private String importPath;
        private Set<String> toolbarItems;
        private Set<String> dependencies;
        private boolean isPremium = false;

        private Builder(String jsName) {
            this.jsName = jsName;
        }

        /**
         * Set import path (e.g., 'my-ckeditor-plugin' or '@scope/my-plugin')
         *
         * @param importPath npm 包名或相对路径
         * @throws IllegalArgumentException 如果路径格式不合法
         */
        public Builder withImportPath(String importPath) {
            if (importPath != null && !importPath.isEmpty()) {
                validateImportPath(importPath);
            }
            this.importPath = importPath;
            return this;
        }

        /**
         * 验证 importPath 是否为合法的 npm 包名或相对路径
         * 防止路径遍历攻击和任意模块加载
         */
        private void validateImportPath(String path) {
            // 禁止绝对路径
            if (path.startsWith("/") || path.matches("^[a-zA-Z]:.*")) {
                throw new IllegalArgumentException(
                    "Absolute paths are not allowed in importPath: " + path);
            }

            // 禁止 URL
            if (path.contains("://")) {
                throw new IllegalArgumentException(
                    "URLs are not allowed in importPath: " + path);
            }

            // 禁止过深的路径遍历 (超过 2 级向上)
            // ../../.. 表示向上 3 级，超过允许的 2 级限制
            if (path.contains("../../..")) {
                throw new IllegalArgumentException(
                    "Deep path traversal (more than 2 levels up) is not allowed in importPath: " + path);
            }

            // 验证格式
            if (!VALID_IMPORT_PATH.matcher(path).matches()) {
                throw new IllegalArgumentException(
                    "Invalid importPath format. Must be a valid npm package name or relative path: " + path);
            }
        }

        /**
         * Set toolbar items provided by this plugin
         */
        public Builder withToolbarItems(String... items) {
            this.toolbarItems = Set.of(items);
            return this;
        }

        /**
         * Set dependency plugins
         */
        public Builder withDependencies(String... deps) {
            this.dependencies = Set.of(deps);
            return this;
        }

        /**
         * Mark as premium feature (import from ckeditor5-premium-features)
         */
        public Builder premium() {
            this.isPremium = true;
            return this;
        }

        /**
         * Build custom plugin
         */
        public CustomPlugin build() {
            return new CustomPlugin(this);
        }
    }
}
