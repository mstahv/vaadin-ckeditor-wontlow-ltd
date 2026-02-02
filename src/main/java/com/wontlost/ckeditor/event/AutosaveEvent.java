package com.wontlost.ckeditor.event;

import com.vaadin.flow.component.ComponentEvent;
import com.wontlost.ckeditor.VaadinCKEditor;

/**
 * 自动保存事件。
 * 当编辑器内容自动保存时触发。
 *
 * <p>使用示例：</p>
 * <pre>
 * editor.addAutosaveListener(event -&gt; {
 *     String content = event.getContent();
 *     // 保存到数据库或后端服务
 *     documentService.save(documentId, content);
 *
 *     if (event.isSuccess()) {
 *         Notification.show("已自动保存");
 *     }
 * });
 * </pre>
 *
 * <p>自动保存行为可通过 Builder 配置：</p>
 * <pre>
 * VaadinCKEditor editor = VaadinCKEditor.create()
 *     .withPreset(CKEditorPreset.STANDARD)
 *     .withAutosave(content -&gt; saveToBackend(content), 3000) // 3秒延迟
 *     .build();
 * </pre>
 */
public class AutosaveEvent extends ComponentEvent<VaadinCKEditor> {

    private final String content;
    private final long timestamp;
    private final boolean success;
    private final String errorMessage;

    /**
     * 创建成功的自动保存事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param content 保存的内容
     */
    public AutosaveEvent(VaadinCKEditor source, boolean fromClient, String content) {
        this(source, fromClient, content, true, null);
    }

    /**
     * 创建自动保存事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param content 保存的内容
     * @param success 是否保存成功
     * @param errorMessage 错误消息（失败时）
     */
    public AutosaveEvent(VaadinCKEditor source, boolean fromClient, String content,
                        boolean success, String errorMessage) {
        super(source, fromClient);
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.success = success;
        this.errorMessage = errorMessage;
    }

    /**
     * 获取保存的内容
     *
     * @return HTML 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取保存时间戳
     *
     * @return 时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * 检查保存是否成功
     *
     * @return 如果保存成功返回 true
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息，如果保存成功则返回 null
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
