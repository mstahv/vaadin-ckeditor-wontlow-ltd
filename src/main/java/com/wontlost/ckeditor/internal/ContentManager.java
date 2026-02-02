package com.wontlost.ckeditor.internal;

import com.wontlost.ckeditor.handler.HtmlSanitizer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 管理编辑器内容的内部类。
 * 处理内容获取、设置、清理和转换。
 *
 * <p>此类是内部 API，不应直接由外部代码使用。</p>
 */
public class ContentManager {

    private final HtmlSanitizer htmlSanitizer;

    /**
     * 创建内容管理器
     *
     * @param htmlSanitizer HTML 清理器，可为 null
     */
    public ContentManager(HtmlSanitizer htmlSanitizer) {
        this.htmlSanitizer = htmlSanitizer;
    }

    /**
     * 获取清理后的 HTML 内容
     *
     * @param html 原始 HTML
     * @return 清理后的 HTML，如果没有设置清理器则返回原始内容
     */
    public String getSanitizedValue(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        if (htmlSanitizer != null) {
            return htmlSanitizer.sanitize(html);
        }
        return html;
    }

    /**
     * 将 HTML 转换为纯文本
     *
     * @param html HTML 内容
     * @return 纯文本内容
     */
    public String getPlainText(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return Jsoup.parse(html).text();
    }

    /**
     * 使用宽松规则清理 HTML
     *
     * @param html HTML 内容
     * @return 清理后的 HTML
     */
    public String getSanitizedHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return Jsoup.clean(html, Safelist.relaxed());
    }

    /**
     * 使用自定义规则清理 HTML
     *
     * @param html HTML 内容
     * @param safelist 清理规则
     * @return 清理后的 HTML
     */
    public String sanitizeHtml(String html, Safelist safelist) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        return Jsoup.clean(html, safelist);
    }

    /**
     * 规范化 HTML 内容用于比较
     *
     * @param html HTML 内容
     * @return 规范化后的 HTML
     */
    public String normalizeForComparison(String html) {
        if (html == null) {
            return "";
        }
        // 移除多余空白，规范化换行
        return html.trim()
            .replaceAll("\\s+", " ")
            .replaceAll(">\\s+<", "><");
    }

    /**
     * 检查内容是否为空
     *
     * @param html HTML 内容
     * @return 如果内容为空或只包含空白标签则返回 true
     */
    public boolean isContentEmpty(String html) {
        if (html == null || html.isEmpty()) {
            return true;
        }
        String text = getPlainText(html);
        return text.trim().isEmpty();
    }

    /**
     * 估算内容的字符数（不包含 HTML 标签）
     *
     * @param html HTML 内容
     * @return 字符数
     */
    public int getCharacterCount(String html) {
        String text = getPlainText(html);
        return text.length();
    }

    /**
     * 估算内容的单词数
     *
     * @param html HTML 内容
     * @return 单词数
     */
    public int getWordCount(String html) {
        String text = getPlainText(html);
        if (text.trim().isEmpty()) {
            return 0;
        }
        // 简单的单词分割，支持中文字符计数
        String[] words = text.trim().split("\\s+");
        int count = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                // 中文字符每个算一个词
                int chineseChars = 0;
                for (char c : word.toCharArray()) {
                    if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                        chineseChars++;
                    }
                }
                count += chineseChars > 0 ? chineseChars : 1;
            }
        }
        return count;
    }
}
