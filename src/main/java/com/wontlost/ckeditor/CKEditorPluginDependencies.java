package com.wontlost.ckeditor;

import java.util.*;

/**
 * Plugin dependencies management for CKEditor 5.
 *
 * <p>This class manages the dependency relationships between CKEditor 5 plugins,
 * ensuring that when a plugin is added, all its required dependencies are also included.
 * Dependencies are gathered from the official CKEditor 5 documentation.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * Set&lt;CKEditorPlugin&gt; plugins = new HashSet&lt;&gt;();
 * plugins.add(CKEditorPlugin.IMAGE_CAPTION);
 * plugins.add(CKEditorPlugin.TABLE_TOOLBAR);
 *
 * // Automatically resolve all dependencies
 * Set&lt;CKEditorPlugin&gt; resolvedPlugins = CKEditorPluginDependencies.resolve(plugins);
 * // resolvedPlugins now includes IMAGE, TABLE, ESSENTIALS, PARAGRAPH, etc.
 * </pre>
 *
 * @see <a href="https://ckeditor.com/docs/ckeditor5/latest/features/index.html">CKEditor 5 Features</a>
 */
public final class CKEditorPluginDependencies {

    /**
     * Dependency graph: maps each plugin to its required dependencies.
     * Key = plugin that has dependencies, Value = set of plugins it depends on.
     */
    private static final Map<CKEditorPlugin, Set<CKEditorPlugin>> DEPENDENCIES;

    /**
     * Recommended plugins: maps plugins to other plugins that enhance functionality.
     * These are not strictly required but provide better user experience.
     */
    private static final Map<CKEditorPlugin, Set<CKEditorPlugin>> RECOMMENDED;

    static {
        // Initialize dependency map
        Map<CKEditorPlugin, Set<CKEditorPlugin>> deps = new EnumMap<>(CKEditorPlugin.class);

        // ==================== Core Dependencies ====================
        // Essentials and Paragraph are foundational - most plugins implicitly need them
        // We'll add them as universal dependencies in the resolve method

        // ==================== Image Plugin Dependencies ====================
        // All image-related plugins depend on the base Image plugin
        deps.put(CKEditorPlugin.IMAGE_TOOLBAR, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_CAPTION, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_STYLE, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_RESIZE, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_UPLOAD, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_INSERT, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_BLOCK, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.IMAGE_INLINE, Set.of(CKEditorPlugin.IMAGE));
        deps.put(CKEditorPlugin.LINK_IMAGE, Set.of(CKEditorPlugin.IMAGE, CKEditorPlugin.LINK));
        deps.put(CKEditorPlugin.AUTO_IMAGE, Set.of(CKEditorPlugin.IMAGE, CKEditorPlugin.CLIPBOARD));

        // ==================== Table Plugin Dependencies ====================
        // All table-related plugins depend on the base Table plugin
        deps.put(CKEditorPlugin.TABLE_TOOLBAR, Set.of(CKEditorPlugin.TABLE));
        deps.put(CKEditorPlugin.TABLE_PROPERTIES, Set.of(CKEditorPlugin.TABLE));
        deps.put(CKEditorPlugin.TABLE_CELL_PROPERTIES, Set.of(CKEditorPlugin.TABLE));
        deps.put(CKEditorPlugin.TABLE_CAPTION, Set.of(CKEditorPlugin.TABLE));
        deps.put(CKEditorPlugin.TABLE_COLUMN_RESIZE, Set.of(CKEditorPlugin.TABLE));

        // ==================== Link Plugin Dependencies ====================
        deps.put(CKEditorPlugin.AUTO_LINK, Set.of(CKEditorPlugin.LINK));

        // ==================== List Plugin Dependencies ====================
        deps.put(CKEditorPlugin.TODO_LIST, Set.of(CKEditorPlugin.LIST));

        // ==================== Indent Plugin Dependencies ====================
        // IndentBlock provides actual indentation behavior for Indent commands
        deps.put(CKEditorPlugin.INDENT_BLOCK, Set.of(CKEditorPlugin.INDENT));

        // ==================== Special Characters Dependencies ====================
        // SpecialCharactersEssentials bundles all character categories
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_ESSENTIALS, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));

        // ==================== HTML Support Dependencies ====================
        // Style plugin requires GeneralHtmlSupport to apply CSS classes
        deps.put(CKEditorPlugin.STYLE, Set.of(CKEditorPlugin.GENERAL_HTML_SUPPORT));
        // HtmlComment requires GeneralHtmlSupport
        deps.put(CKEditorPlugin.HTML_COMMENT, Set.of(CKEditorPlugin.GENERAL_HTML_SUPPORT));

        // ==================== Media Dependencies ====================
        // HtmlEmbed works better with GeneralHtmlSupport
        deps.put(CKEditorPlugin.HTML_EMBED, Set.of(CKEditorPlugin.GENERAL_HTML_SUPPORT));

        // ==================== Source Editing Dependencies ====================
        // SourceEditing works best with GeneralHtmlSupport to preserve HTML
        deps.put(CKEditorPlugin.SOURCE_EDITING, Set.of(CKEditorPlugin.GENERAL_HTML_SUPPORT));

        // ==================== Upload Adapter Dependencies ====================
        // Upload adapters are typically used with Image plugins
        deps.put(CKEditorPlugin.SIMPLE_UPLOAD_ADAPTER, Set.of(CKEditorPlugin.IMAGE_UPLOAD));
        deps.put(CKEditorPlugin.BASE64_UPLOAD_ADAPTER, Set.of(CKEditorPlugin.IMAGE_UPLOAD));

        // ==================== Premium Feature Dependencies ====================
        // Minimap requires DecoupledEditor (handled at editor type level)
        // ExportPdf, ExportWord, ImportWord are standalone premium features

        // ==================== Restricted Editing Dependencies ====================
        deps.put(CKEditorPlugin.STANDARD_EDITING_MODE, Set.of(CKEditorPlugin.RESTRICTED_EDITING_MODE));

        // ==================== Cloud Services Dependencies ====================
        // CloudServicesUploadAdapter requires CloudServices
        deps.put(CKEditorPlugin.CLOUD_SERVICES_UPLOAD_ADAPTER, Set.of(CKEditorPlugin.CLOUD_SERVICES));
        // CloudServices requires CloudServicesCore
        deps.put(CKEditorPlugin.CLOUD_SERVICES, Set.of(CKEditorPlugin.CLOUD_SERVICES_CORE));

        // ==================== Easy Image Dependencies ====================
        // EasyImage requires CloudServices and ImageUpload
        deps.put(CKEditorPlugin.EASY_IMAGE, Set.of(CKEditorPlugin.CLOUD_SERVICES, CKEditorPlugin.IMAGE_UPLOAD));

        // ==================== Minimap Dependencies ====================
        // Minimap requires Widget
        deps.put(CKEditorPlugin.MINIMAP, Set.of(CKEditorPlugin.WIDGET));

        // ==================== Emoji Dependencies ====================
        // EmojiPicker requires Emoji
        deps.put(CKEditorPlugin.EMOJI_PICKER, Set.of(CKEditorPlugin.EMOJI));

        // ==================== Special Characters Dependencies ====================
        // All special character categories require SpecialCharacters
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_ARROWS, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_CURRENCY, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_LATIN, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_MATHEMATICAL, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));
        deps.put(CKEditorPlugin.SPECIAL_CHARACTERS_TEXT, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS));

        // ==================== List Dependencies ====================
        // ListProperties and ListFormatting require List
        deps.put(CKEditorPlugin.LIST_PROPERTIES, Set.of(CKEditorPlugin.LIST));
        deps.put(CKEditorPlugin.LIST_FORMATTING, Set.of(CKEditorPlugin.LIST));
        deps.put(CKEditorPlugin.ADJACENT_LISTS_SUPPORT, Set.of(CKEditorPlugin.LIST));

        DEPENDENCIES = Collections.unmodifiableMap(deps);

        // Initialize recommended plugins map
        Map<CKEditorPlugin, Set<CKEditorPlugin>> rec = new EnumMap<>(CKEditorPlugin.class);

        // Image feature recommendations
        rec.put(CKEditorPlugin.IMAGE, Set.of(
            CKEditorPlugin.IMAGE_TOOLBAR,
            CKEditorPlugin.IMAGE_CAPTION,
            CKEditorPlugin.IMAGE_STYLE,
            CKEditorPlugin.IMAGE_RESIZE
        ));

        // Table feature recommendations
        rec.put(CKEditorPlugin.TABLE, Set.of(
            CKEditorPlugin.TABLE_TOOLBAR,
            CKEditorPlugin.TABLE_PROPERTIES,
            CKEditorPlugin.TABLE_CELL_PROPERTIES
        ));

        // Link feature recommendations
        rec.put(CKEditorPlugin.LINK, Set.of(CKEditorPlugin.AUTO_LINK));

        // Heading works well with Paragraph (already core)
        rec.put(CKEditorPlugin.HEADING, Set.of(CKEditorPlugin.PARAGRAPH));

        // CodeBlock works well with Autoformat for markdown-style input
        rec.put(CKEditorPlugin.CODE_BLOCK, Set.of(CKEditorPlugin.AUTOFORMAT));

        // SpecialCharacters should have essentials
        rec.put(CKEditorPlugin.SPECIAL_CHARACTERS, Set.of(CKEditorPlugin.SPECIAL_CHARACTERS_ESSENTIALS));

        // Source editing recommendations
        rec.put(CKEditorPlugin.SOURCE_EDITING, Set.of(
            CKEditorPlugin.GENERAL_HTML_SUPPORT,
            CKEditorPlugin.HTML_EMBED
        ));

        // Style feature recommendations
        rec.put(CKEditorPlugin.STYLE, Set.of(CKEditorPlugin.GENERAL_HTML_SUPPORT));

        // Indent works better with IndentBlock for paragraphs
        rec.put(CKEditorPlugin.INDENT, Set.of(CKEditorPlugin.INDENT_BLOCK));

        // Cloud services recommendations
        rec.put(CKEditorPlugin.CLOUD_SERVICES, Set.of(
            CKEditorPlugin.CLOUD_SERVICES_UPLOAD_ADAPTER
        ));

        // Emoji recommendations
        rec.put(CKEditorPlugin.EMOJI, Set.of(CKEditorPlugin.EMOJI_PICKER));

        // List feature recommendations
        rec.put(CKEditorPlugin.LIST, Set.of(
            CKEditorPlugin.LIST_PROPERTIES,
            CKEditorPlugin.TODO_LIST
        ));

        RECOMMENDED = Collections.unmodifiableMap(rec);
    }

    private CKEditorPluginDependencies() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the direct dependencies for a plugin.
     *
     * @param plugin the plugin to get dependencies for
     * @return set of direct dependencies (never null, may be empty)
     */
    public static Set<CKEditorPlugin> getDependencies(CKEditorPlugin plugin) {
        return DEPENDENCIES.getOrDefault(plugin, Collections.emptySet());
    }

    /**
     * Get the recommended plugins for a plugin.
     * These are not required but enhance the functionality.
     *
     * @param plugin the plugin to get recommendations for
     * @return set of recommended plugins (never null, may be empty)
     */
    public static Set<CKEditorPlugin> getRecommended(CKEditorPlugin plugin) {
        return RECOMMENDED.getOrDefault(plugin, Collections.emptySet());
    }

    /**
     * Check if a plugin has dependencies.
     *
     * @param plugin the plugin to check
     * @return true if the plugin has dependencies
     */
    public static boolean hasDependencies(CKEditorPlugin plugin) {
        return DEPENDENCIES.containsKey(plugin) && !DEPENDENCIES.get(plugin).isEmpty();
    }

    /**
     * Resolve all dependencies for a set of plugins.
     * This method transitively resolves all dependencies, ensuring that
     * if plugin A depends on B, and B depends on C, all three are included.
     *
     * <p>Core plugins (ESSENTIALS and PARAGRAPH) are always included.</p>
     *
     * @param plugins the initial set of plugins
     * @return a new set containing all plugins with their dependencies resolved
     */
    public static Set<CKEditorPlugin> resolve(Set<CKEditorPlugin> plugins) {
        return resolve(plugins, true);
    }

    /**
     * Resolve all dependencies for a set of plugins.
     *
     * @param plugins the initial set of plugins
     * @param includeCorePlugins whether to automatically include ESSENTIALS and PARAGRAPH
     * @return a new set containing all plugins with their dependencies resolved
     */
    public static Set<CKEditorPlugin> resolve(Set<CKEditorPlugin> plugins, boolean includeCorePlugins) {
        Set<CKEditorPlugin> resolved = EnumSet.noneOf(CKEditorPlugin.class);

        // Add core plugins if requested
        if (includeCorePlugins) {
            resolved.add(CKEditorPlugin.ESSENTIALS);
            resolved.add(CKEditorPlugin.PARAGRAPH);
        }

        // Add all requested plugins and resolve their dependencies
        for (CKEditorPlugin plugin : plugins) {
            resolveTransitive(plugin, resolved);
        }

        return resolved;
    }

    /**
     * Resolve all dependencies including recommended plugins.
     *
     * @param plugins the initial set of plugins
     * @return a new set containing all plugins with dependencies and recommendations
     */
    public static Set<CKEditorPlugin> resolveWithRecommended(Set<CKEditorPlugin> plugins) {
        Set<CKEditorPlugin> resolved = resolve(plugins, true);

        // Add recommended plugins for each resolved plugin
        Set<CKEditorPlugin> withRecommended = EnumSet.copyOf(resolved);
        for (CKEditorPlugin plugin : resolved) {
            Set<CKEditorPlugin> recommended = RECOMMENDED.get(plugin);
            if (recommended != null) {
                for (CKEditorPlugin rec : recommended) {
                    resolveTransitive(rec, withRecommended);
                }
            }
        }

        return withRecommended;
    }

    /**
     * Recursively resolve dependencies for a single plugin.
     */
    private static void resolveTransitive(CKEditorPlugin plugin, Set<CKEditorPlugin> resolved) {
        if (resolved.contains(plugin)) {
            return; // Already resolved, avoid cycles
        }

        // First resolve all dependencies
        Set<CKEditorPlugin> deps = DEPENDENCIES.get(plugin);
        if (deps != null) {
            for (CKEditorPlugin dep : deps) {
                resolveTransitive(dep, resolved);
            }
        }

        // Then add this plugin
        resolved.add(plugin);
    }

    /**
     * Get all plugins that depend on the specified plugin.
     *
     * @param plugin the plugin to find dependents for
     * @return set of plugins that depend on the given plugin
     */
    public static Set<CKEditorPlugin> getDependents(CKEditorPlugin plugin) {
        Set<CKEditorPlugin> dependents = EnumSet.noneOf(CKEditorPlugin.class);
        for (Map.Entry<CKEditorPlugin, Set<CKEditorPlugin>> entry : DEPENDENCIES.entrySet()) {
            if (entry.getValue().contains(plugin)) {
                dependents.add(entry.getKey());
            }
        }
        return dependents;
    }

    /**
     * Check if removing a plugin would break any dependencies.
     *
     * @param plugin the plugin to check
     * @param currentPlugins the current set of plugins
     * @return set of plugins that would be broken if the plugin is removed
     */
    public static Set<CKEditorPlugin> checkRemovalImpact(CKEditorPlugin plugin, Set<CKEditorPlugin> currentPlugins) {
        Set<CKEditorPlugin> broken = EnumSet.noneOf(CKEditorPlugin.class);
        for (CKEditorPlugin p : currentPlugins) {
            Set<CKEditorPlugin> deps = DEPENDENCIES.get(p);
            if (deps != null && deps.contains(plugin)) {
                broken.add(p);
            }
        }
        return broken;
    }

    /**
     * Validate that all dependencies are satisfied for a set of plugins.
     *
     * @param plugins the plugins to validate
     * @return map of plugins to their missing dependencies (empty if all satisfied)
     */
    public static Map<CKEditorPlugin, Set<CKEditorPlugin>> validateDependencies(Set<CKEditorPlugin> plugins) {
        Map<CKEditorPlugin, Set<CKEditorPlugin>> missing = new EnumMap<>(CKEditorPlugin.class);

        for (CKEditorPlugin plugin : plugins) {
            Set<CKEditorPlugin> deps = DEPENDENCIES.get(plugin);
            if (deps != null) {
                Set<CKEditorPlugin> missingDeps = EnumSet.noneOf(CKEditorPlugin.class);
                for (CKEditorPlugin dep : deps) {
                    if (!plugins.contains(dep)) {
                        missingDeps.add(dep);
                    }
                }
                if (!missingDeps.isEmpty()) {
                    missing.put(plugin, missingDeps);
                }
            }
        }

        return missing;
    }

    /**
     * Get a topologically sorted list of plugins.
     * Dependencies are listed before the plugins that depend on them.
     *
     * @param plugins the plugins to sort
     * @return list of plugins in dependency order
     */
    public static List<CKEditorPlugin> topologicalSort(Set<CKEditorPlugin> plugins) {
        List<CKEditorPlugin> sorted = new ArrayList<>();
        Set<CKEditorPlugin> visited = EnumSet.noneOf(CKEditorPlugin.class);
        Set<CKEditorPlugin> visiting = EnumSet.noneOf(CKEditorPlugin.class);

        for (CKEditorPlugin plugin : plugins) {
            if (!visited.contains(plugin)) {
                topologicalSortVisit(plugin, plugins, visited, visiting, sorted);
            }
        }

        return sorted;
    }

    private static void topologicalSortVisit(
            CKEditorPlugin plugin,
            Set<CKEditorPlugin> plugins,
            Set<CKEditorPlugin> visited,
            Set<CKEditorPlugin> visiting,
            List<CKEditorPlugin> sorted) {

        if (visiting.contains(plugin)) {
            // Cycle detected - shouldn't happen with proper dependency graph
            return;
        }
        if (visited.contains(plugin)) {
            return;
        }

        visiting.add(plugin);

        Set<CKEditorPlugin> deps = DEPENDENCIES.get(plugin);
        if (deps != null) {
            for (CKEditorPlugin dep : deps) {
                if (plugins.contains(dep)) {
                    topologicalSortVisit(dep, plugins, visited, visiting, sorted);
                }
            }
        }

        visiting.remove(plugin);
        visited.add(plugin);
        sorted.add(plugin);
    }

    /**
     * Get a human-readable dependency tree for a plugin.
     *
     * @param plugin the plugin to get the tree for
     * @return formatted string showing the dependency tree
     */
    public static String getDependencyTree(CKEditorPlugin plugin) {
        StringBuilder sb = new StringBuilder();
        buildDependencyTree(plugin, sb, "", true, EnumSet.noneOf(CKEditorPlugin.class));
        return sb.toString();
    }

    private static void buildDependencyTree(
            CKEditorPlugin plugin,
            StringBuilder sb,
            String prefix,
            boolean isLast,
            Set<CKEditorPlugin> visited) {

        sb.append(prefix);
        sb.append(isLast ? "└── " : "├── ");
        sb.append(plugin.getJsName());

        if (visited.contains(plugin)) {
            sb.append(" (circular)");
            sb.append("\n");
            return;
        }

        sb.append("\n");
        visited.add(plugin);

        Set<CKEditorPlugin> deps = DEPENDENCIES.get(plugin);
        if (deps != null && !deps.isEmpty()) {
            List<CKEditorPlugin> depList = new ArrayList<>(deps);
            for (int i = 0; i < depList.size(); i++) {
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                buildDependencyTree(depList.get(i), sb, newPrefix, i == depList.size() - 1, visited);
            }
        }

        visited.remove(plugin);
    }

    /**
     * Get all plugins in dependency order suitable for loading.
     * This is a convenience method that resolves and sorts plugins.
     *
     * @param plugins the plugins to process
     * @return list of all required plugins in load order
     */
    public static List<CKEditorPlugin> getLoadOrder(Set<CKEditorPlugin> plugins) {
        Set<CKEditorPlugin> resolved = resolve(plugins);
        return topologicalSort(resolved);
    }
}
