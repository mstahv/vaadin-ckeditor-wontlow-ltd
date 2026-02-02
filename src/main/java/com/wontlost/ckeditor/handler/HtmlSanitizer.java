package com.wontlost.ckeditor.handler;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * HTML 内容清理器。
 * 用于在保存或显示内容前清理危险的 HTML 标签和属性。
 *
 * <p>使用示例：</p>
 * <pre>
 * // 使用预定义策略
 * editor.setHtmlSanitizer(HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT));
 *
 * // 自定义清理逻辑
 * editor.setHtmlSanitizer(html -&gt; {
 *     // 移除所有 script 标签
 *     return html.replaceAll("&lt;script[^&gt;]*&gt;.*?&lt;/script&gt;", "");
 * });
 * </pre>
 *
 * @see SanitizationPolicy
 */
@FunctionalInterface
public interface HtmlSanitizer {

    /**
     * 清理 HTML 内容
     *
     * @param html 原始 HTML 内容
     * @return 清理后的安全 HTML
     */
    String sanitize(String html);

    /**
     * 清理策略
     */
    enum SanitizationPolicy {
        /**
         * 不清理，保留原始内容
         */
        NONE,

        /**
         * 基础清理：移除脚本和危险标签
         */
        BASIC,

        /**
         * 宽松清理：允许大多数格式化标签
         */
        RELAXED,

        /**
         * 严格清理：只保留基本文本格式
         */
        STRICT
    }

    /**
     * 创建基于策略的清理器
     *
     * @param policy 清理策略
     * @return 清理器实例
     */
    static HtmlSanitizer withPolicy(SanitizationPolicy policy) {
        return html -> {
            if (html == null || html.isEmpty()) {
                return "";
            }

            switch (policy) {
                case NONE:
                    return html;

                case BASIC:
                    return Jsoup.clean(html, Safelist.basic());

                case RELAXED:
                    return Jsoup.clean(html, Safelist.relaxed());

                case STRICT:
                    // 只允许基本格式化
                    Safelist strict = new Safelist()
                        .addTags("p", "br", "b", "i", "u", "strong", "em")
                        .addTags("h1", "h2", "h3", "h4", "h5", "h6")
                        .addTags("ul", "ol", "li")
                        .addTags("blockquote", "pre", "code");
                    return Jsoup.clean(html, strict);

                default:
                    return Jsoup.clean(html, Safelist.basic());
            }
        };
    }

    /**
     * 创建自定义白名单的清理器
     *
     * @param safelist Jsoup 白名单配置
     * @return 清理器实例
     */
    static HtmlSanitizer withSafelist(Safelist safelist) {
        return html -> {
            if (html == null || html.isEmpty()) {
                return "";
            }
            return Jsoup.clean(html, safelist);
        };
    }

    /**
     * 组合清理器（链式执行）
     *
     * @param other 另一个清理器
     * @return 组合后的清理器
     */
    default HtmlSanitizer andThen(HtmlSanitizer other) {
        return html -> other.sanitize(this.sanitize(html));
    }

    /**
     * 不进行任何清理的清理器
     *
     * @return 透传清理器
     */
    static HtmlSanitizer passthrough() {
        return html -> html;
    }
}
