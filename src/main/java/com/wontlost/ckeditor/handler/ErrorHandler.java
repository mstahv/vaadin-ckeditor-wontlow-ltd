package com.wontlost.ckeditor.handler;

import com.wontlost.ckeditor.event.EditorErrorEvent.EditorError;

/**
 * 编辑器错误处理器。
 * 用于自定义错误处理逻辑，如记录日志、发送告警等。
 *
 * <h2>返回值语义</h2>
 * <p>{@link #handleError(EditorError)} 方法的返回值决定错误的传播行为：</p>
 * <ul>
 *   <li>{@code true} - 错误已完全处理，<b>停止传播</b>，不会触发 {@code EditorErrorEvent}</li>
 *   <li>{@code false} - 错误未完全处理，<b>继续传播</b>，将触发 {@code EditorErrorEvent}</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 *
 * <h3>基础用法 - 记录日志但不阻止传播</h3>
 * <pre>
 * editor.setErrorHandler(error -&gt; {
 *     logger.error("CKEditor error [{}]: {}", error.getCode(), error.getMessage());
 *     return false; // 继续传播，允许其他监听器处理
 * });
 * </pre>
 *
 * <h3>条件处理 - 只拦截特定错误</h3>
 * <pre>
 * editor.setErrorHandler(error -&gt; {
 *     if ("NETWORK_ERROR".equals(error.getCode())) {
 *         // 网络错误自动重试，不传播给用户
 *         retryService.scheduleRetry();
 *         return true; // 已处理，停止传播
 *     }
 *     return false; // 其他错误继续传播
 * });
 * </pre>
 *
 * <h3>链式处理 - 组合多个处理器</h3>
 * <pre>
 * ErrorHandler logger = ErrorHandler.logging(log);
 * ErrorHandler alerter = error -&gt; {
 *     if (error.getSeverity() == ErrorSeverity.FATAL) {
 *         alertService.sendAlert(error.getMessage());
 *         return true;
 *     }
 *     return false;
 * };
 * // 先记录日志，再发送告警
 * editor.setErrorHandler(ErrorHandler.compose(logger, alerter));
 * </pre>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li>日志记录器通常应返回 {@code false}，允许其他处理器继续处理</li>
 *   <li>只有在错误被完全解决（如自动重试成功）时才返回 {@code true}</li>
 *   <li>对于致命错误 ({@code FATAL})，通常应让其传播以便 UI 能够响应</li>
 *   <li>使用 {@link #compose(ErrorHandler...)} 组合多个处理器时，顺序很重要</li>
 * </ul>
 *
 * @see EditorError
 * @see com.wontlost.ckeditor.event.EditorErrorEvent
 */
@FunctionalInterface
public interface ErrorHandler {

    /**
     * 处理编辑器错误。
     *
     * <p>此方法在 {@code EditorErrorEvent} 触发<b>之前</b>被调用，
     * 提供了拦截和处理错误的机会。</p>
     *
     * @param error 错误详情，包含错误代码、消息、严重程度等信息
     * @return {@code true} 表示错误已处理完毕，停止传播（不触发事件）；
     *         {@code false} 表示错误未处理或需要继续传播（触发 {@code EditorErrorEvent}）
     */
    boolean handleError(EditorError error);

    /**
     * 创建记录日志的错误处理器
     *
     * @param logger 日志记录器
     * @return 错误处理器实例
     */
    static ErrorHandler logging(java.util.logging.Logger logger) {
        return error -> {
            switch (error.getSeverity()) {
                case WARNING:
                    logger.warning(() -> String.format("[%s] %s", error.getCode(), error.getMessage()));
                    break;
                case ERROR:
                    logger.severe(() -> String.format("[%s] %s", error.getCode(), error.getMessage()));
                    break;
                case FATAL:
                    logger.severe(() -> String.format("FATAL [%s] %s\n%s",
                        error.getCode(), error.getMessage(), error.getStackTrace()));
                    break;
            }
            return false; // 继续传播
        };
    }

    /**
     * 组合多个错误处理器
     *
     * @param handlers 处理器列表
     * @return 组合后的处理器
     */
    static ErrorHandler compose(ErrorHandler... handlers) {
        return error -> {
            for (ErrorHandler handler : handlers) {
                if (handler.handleError(error)) {
                    return true; // 错误已处理
                }
            }
            return false;
        };
    }
}
