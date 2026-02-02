package com.wontlost.ckeditor.internal;

import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.event.*;
import com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource;
import com.wontlost.ckeditor.event.EditorErrorEvent.EditorError;
import com.wontlost.ckeditor.event.FallbackEvent.FallbackMode;
import com.wontlost.ckeditor.handler.ErrorHandler;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 管理编辑器事件分发的内部类。
 * 提供类型安全的事件注册和分发机制。
 *
 * <p>此类是内部 API，不应直接由外部代码使用。</p>
 */
public class EventDispatcher {

    private static final Logger logger = Logger.getLogger(EventDispatcher.class.getName());

    private final VaadinCKEditor source;
    private ErrorHandler errorHandler;

    // 使用 CopyOnWriteArrayList 保证线程安全
    private final List<ComponentEventListener<EditorReadyEvent>> readyListeners = new CopyOnWriteArrayList<>();
    private final List<ComponentEventListener<EditorErrorEvent>> errorListeners = new CopyOnWriteArrayList<>();
    private final List<ComponentEventListener<AutosaveEvent>> autosaveListeners = new CopyOnWriteArrayList<>();
    private final List<ComponentEventListener<ContentChangeEvent>> contentChangeListeners = new CopyOnWriteArrayList<>();
    private final List<ComponentEventListener<FallbackEvent>> fallbackListeners = new CopyOnWriteArrayList<>();

    /**
     * 创建事件分发器
     *
     * @param source 事件源组件
     */
    public EventDispatcher(VaadinCKEditor source) {
        this.source = source;
    }

    /**
     * 设置错误处理器
     *
     * @param errorHandler 错误处理器
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * 获取错误处理器
     *
     * @return 错误处理器
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    // ==================== 监听器注册 ====================

    /**
     * 添加编辑器就绪监听器
     *
     * @param listener 监听器
     * @return 注册句柄，用于移除监听器
     */
    public Registration addEditorReadyListener(ComponentEventListener<EditorReadyEvent> listener) {
        readyListeners.add(listener);
        return () -> readyListeners.remove(listener);
    }

    /**
     * 添加错误监听器
     *
     * @param listener 监听器
     * @return 注册句柄
     */
    public Registration addEditorErrorListener(ComponentEventListener<EditorErrorEvent> listener) {
        errorListeners.add(listener);
        return () -> errorListeners.remove(listener);
    }

    /**
     * 添加自动保存监听器
     *
     * @param listener 监听器
     * @return 注册句柄
     */
    public Registration addAutosaveListener(ComponentEventListener<AutosaveEvent> listener) {
        autosaveListeners.add(listener);
        return () -> autosaveListeners.remove(listener);
    }

    /**
     * 添加内容变更监听器
     *
     * @param listener 监听器
     * @return 注册句柄
     */
    public Registration addContentChangeListener(ComponentEventListener<ContentChangeEvent> listener) {
        contentChangeListeners.add(listener);
        return () -> contentChangeListeners.remove(listener);
    }

    /**
     * 添加回退模式监听器
     *
     * @param listener 监听器
     * @return 注册句柄
     */
    public Registration addFallbackListener(ComponentEventListener<FallbackEvent> listener) {
        fallbackListeners.add(listener);
        return () -> fallbackListeners.remove(listener);
    }

    // ==================== 事件触发 ====================

    /**
     * 触发编辑器就绪事件
     *
     * @param initTimeMs 初始化耗时（毫秒）
     */
    public void fireEditorReady(long initTimeMs) {
        EditorReadyEvent event = new EditorReadyEvent(source, true, initTimeMs);
        dispatchEvent(readyListeners, event, "EditorReady");
    }

    /**
     * 触发错误事件
     *
     * @param error 错误信息
     * @return 如果错误被处理器处理则返回 true
     */
    public boolean fireEditorError(EditorError error) {
        // 先调用错误处理器
        if (errorHandler != null) {
            try {
                if (errorHandler.handleError(error)) {
                    return true; // 错误已处理
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error in error handler", e);
            }
        }

        // 触发事件
        EditorErrorEvent event = new EditorErrorEvent(source, true, error);
        dispatchEvent(errorListeners, event, "EditorError");
        return false;
    }

    /**
     * 触发自动保存事件
     *
     * @param content 保存的内容
     * @param success 是否成功
     * @param errorMessage 错误消息（成功时为 null）
     */
    public void fireAutosave(String content, boolean success, String errorMessage) {
        AutosaveEvent event = new AutosaveEvent(source, true, content, success, errorMessage);
        dispatchEvent(autosaveListeners, event, "Autosave");
    }

    /**
     * 触发内容变更事件
     *
     * @param oldContent 旧内容
     * @param newContent 新内容
     * @param changeSource 变更来源
     */
    public void fireContentChange(String oldContent, String newContent, ChangeSource changeSource) {
        ContentChangeEvent event = new ContentChangeEvent(source, true, oldContent, newContent, changeSource);
        dispatchEvent(contentChangeListeners, event, "ContentChange");
    }

    /**
     * 触发回退模式事件
     *
     * @param mode 回退模式
     * @param reason 原因
     * @param originalError 原始错误
     */
    public void fireFallback(FallbackMode mode, String reason, String originalError) {
        FallbackEvent event = new FallbackEvent(source, true, mode, reason, originalError);
        dispatchEvent(fallbackListeners, event, "Fallback");
    }

    // ==================== 内部方法 ====================

    private <E extends ComponentEvent<?>> void dispatchEvent(List<ComponentEventListener<E>> listeners, E event, String eventName) {
        for (ComponentEventListener<E> listener : listeners) {
            try {
                listener.onComponentEvent(event);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error in " + eventName + " listener", e);
            }
        }
    }

    /**
     * 清理所有监听器
     */
    public void cleanup() {
        readyListeners.clear();
        errorListeners.clear();
        autosaveListeners.clear();
        contentChangeListeners.clear();
        fallbackListeners.clear();
    }

    /**
     * 获取已注册监听器的统计信息
     *
     * @return 监听器统计
     */
    public ListenerStats getListenerStats() {
        return new ListenerStats(
            readyListeners.size(),
            errorListeners.size(),
            autosaveListeners.size(),
            contentChangeListeners.size(),
            fallbackListeners.size()
        );
    }

    /**
     * 监听器统计信息
     */
    public static class ListenerStats {
        public final int ready;
        public final int error;
        public final int autosave;
        public final int contentChange;
        public final int fallback;

        ListenerStats(int ready, int error, int autosave, int contentChange, int fallback) {
            this.ready = ready;
            this.error = error;
            this.autosave = autosave;
            this.contentChange = contentChange;
            this.fallback = fallback;
        }

        public int total() {
            return ready + error + autosave + contentChange + fallback;
        }
    }
}
