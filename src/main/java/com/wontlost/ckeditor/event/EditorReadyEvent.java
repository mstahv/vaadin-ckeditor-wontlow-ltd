package com.wontlost.ckeditor.event;

import com.vaadin.flow.component.ComponentEvent;
import com.wontlost.ckeditor.VaadinCKEditor;

/**
 * 编辑器就绪事件。
 * 当 CKEditor 实例完全初始化并准备好接受用户输入时触发。
 *
 * <p>使用示例：</p>
 * <pre>
 * editor.addEditorReadyListener(event -&gt; {
 *     // 编辑器已就绪，可以安全执行操作
 *     event.getSource().focus();
 * });
 * </pre>
 */
public class EditorReadyEvent extends ComponentEvent<VaadinCKEditor> {

    private final long initializationTimeMs;

    /**
     * 创建编辑器就绪事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param initializationTimeMs 编辑器初始化耗时（毫秒）
     */
    public EditorReadyEvent(VaadinCKEditor source, boolean fromClient, long initializationTimeMs) {
        super(source, fromClient);
        this.initializationTimeMs = initializationTimeMs;
    }

    /**
     * 获取编辑器初始化耗时
     *
     * @return 初始化耗时（毫秒）
     */
    public long getInitializationTimeMs() {
        return initializationTimeMs;
    }
}
