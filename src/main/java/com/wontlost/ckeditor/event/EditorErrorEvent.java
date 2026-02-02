package com.wontlost.ckeditor.event;

import com.vaadin.flow.component.ComponentEvent;
import com.wontlost.ckeditor.VaadinCKEditor;

/**
 * 编辑器错误事件。
 * 当编辑器遇到错误时触发，包括初始化错误、运行时错误等。
 *
 * <p>使用示例：</p>
 * <pre>
 * editor.addEditorErrorListener(event -&gt; {
 *     EditorError error = event.getError();
 *     logger.error("Editor error [{}]: {}", error.getCode(), error.getMessage());
 *     if (error.isRecoverable()) {
 *         // 尝试恢复
 *     }
 * });
 * </pre>
 */
public class EditorErrorEvent extends ComponentEvent<VaadinCKEditor> {

    private final EditorError error;

    /**
     * 创建编辑器错误事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param error 错误详情
     */
    public EditorErrorEvent(VaadinCKEditor source, boolean fromClient, EditorError error) {
        super(source, fromClient);
        this.error = error;
    }

    /**
     * 获取错误详情
     *
     * @return 错误对象
     */
    public EditorError getError() {
        return error;
    }

    /**
     * 编辑器错误详情
     */
    public static class EditorError {
        private final String code;
        private final String message;
        private final ErrorSeverity severity;
        private final boolean recoverable;
        private final String stackTrace;

        /**
         * 创建错误详情
         *
         * @param code 错误代码
         * @param message 错误消息
         * @param severity 严重程度
         * @param recoverable 是否可恢复
         * @param stackTrace 堆栈跟踪（可选）
         */
        public EditorError(String code, String message, ErrorSeverity severity,
                          boolean recoverable, String stackTrace) {
            this.code = code;
            this.message = message;
            this.severity = severity;
            this.recoverable = recoverable;
            this.stackTrace = stackTrace;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public ErrorSeverity getSeverity() {
            return severity;
        }

        public boolean isRecoverable() {
            return recoverable;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        @Override
        public String toString() {
            return String.format("EditorError[code=%s, severity=%s, message=%s]",
                code, severity, message);
        }
    }

    /**
     * 错误严重程度
     */
    public enum ErrorSeverity {
        /** 警告级别，不影响编辑器功能 */
        WARNING,
        /** 错误级别，部分功能受影响 */
        ERROR,
        /** 致命级别，编辑器无法正常工作 */
        FATAL
    }
}
