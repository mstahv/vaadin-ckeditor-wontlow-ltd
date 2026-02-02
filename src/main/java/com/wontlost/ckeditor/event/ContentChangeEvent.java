package com.wontlost.ckeditor.event;

import com.vaadin.flow.component.ComponentEvent;
import com.wontlost.ckeditor.VaadinCKEditor;

/**
 * 内容变更事件。
 * 当编辑器内容发生变化时触发，提供变更前后的内容。
 *
 * <p>使用示例：</p>
 * <pre>
 * editor.addContentChangeListener(event -&gt; {
 *     // 计算差异
 *     int charDiff = event.getNewContent().length() - event.getOldContent().length();
 *     updateCharacterCount(charDiff);
 *
 *     // 标记为未保存
 *     markAsUnsaved();
 * });
 * </pre>
 *
 * <p>注意：此事件与 Vaadin 的 ValueChangeListener 不同：</p>
 * <ul>
 *   <li>ContentChangeEvent - 每次内容变化都触发（实时）</li>
 *   <li>ValueChangeListener - 失去焦点或同步时触发</li>
 * </ul>
 */
public class ContentChangeEvent extends ComponentEvent<VaadinCKEditor> {

    private final String oldContent;
    private final String newContent;
    private final ChangeSource changeSource;

    /**
     * 创建内容变更事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param oldContent 变更前的内容
     * @param newContent 变更后的内容
     * @param changeSource 变更来源
     */
    public ContentChangeEvent(VaadinCKEditor source, boolean fromClient,
                              String oldContent, String newContent, ChangeSource changeSource) {
        super(source, fromClient);
        this.oldContent = oldContent;
        this.newContent = newContent;
        this.changeSource = changeSource;
    }

    /**
     * 获取变更前的内容
     *
     * @return 旧内容
     */
    public String getOldContent() {
        return oldContent;
    }

    /**
     * 获取变更后的内容
     *
     * @return 新内容
     */
    public String getNewContent() {
        return newContent;
    }

    /**
     * 获取变更来源
     *
     * @return 变更来源类型
     */
    public ChangeSource getChangeSource() {
        return changeSource;
    }

    /**
     * 检查内容是否实际发生了变化
     *
     * @return 如果内容不同返回 true
     */
    public boolean hasChanged() {
        if (oldContent == null) {
            return newContent != null;
        }
        return !oldContent.equals(newContent);
    }

    /**
     * 获取内容长度变化
     *
     * @return 字符数变化（正数表示增加，负数表示减少）
     */
    public int getLengthDelta() {
        int oldLen = oldContent != null ? oldContent.length() : 0;
        int newLen = newContent != null ? newContent.length() : 0;
        return newLen - oldLen;
    }

    /**
     * 内容变更来源
     */
    public enum ChangeSource {
        /** 用户输入 */
        USER_INPUT,
        /** API 调用（如 setValue） */
        API,
        /** 撤销/重做操作 */
        UNDO_REDO,
        /** 粘贴操作 */
        PASTE,
        /** 协作编辑同步 */
        COLLABORATION,
        /** 未知来源 */
        UNKNOWN
    }
}
