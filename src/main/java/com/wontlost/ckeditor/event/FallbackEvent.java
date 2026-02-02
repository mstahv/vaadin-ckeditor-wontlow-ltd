package com.wontlost.ckeditor.event;

import com.vaadin.flow.component.ComponentEvent;
import com.wontlost.ckeditor.VaadinCKEditor;

/**
 * 降级事件。
 * 当编辑器因错误触发降级模式时发送。
 *
 * <p>使用示例：</p>
 * <pre>
 * editor.addFallbackListener(event -&gt; {
 *     if (event.getMode() == FallbackMode.TEXTAREA) {
 *         // 编辑器已降级为纯文本区域
 *         Notification.show("编辑器加载失败，已切换到基础模式",
 *             Notification.Type.WARNING_MESSAGE);
 *     }
 *
 *     // 记录降级原因
 *     logger.warn("Editor fallback triggered: {}", event.getReason());
 * });
 * </pre>
 */
public class FallbackEvent extends ComponentEvent<VaadinCKEditor> {

    private final FallbackMode mode;
    private final String reason;
    private final String originalError;

    /**
     * 创建降级事件
     *
     * @param source 触发事件的编辑器组件
     * @param fromClient 事件是否来自客户端
     * @param mode 降级模式
     * @param reason 降级原因描述
     * @param originalError 原始错误信息
     */
    public FallbackEvent(VaadinCKEditor source, boolean fromClient,
                        FallbackMode mode, String reason, String originalError) {
        super(source, fromClient);
        this.mode = mode;
        this.reason = reason;
        this.originalError = originalError;
    }

    /**
     * 获取降级模式
     *
     * @return 当前降级模式
     */
    public FallbackMode getMode() {
        return mode;
    }

    /**
     * 获取降级原因
     *
     * @return 人类可读的降级原因
     */
    public String getReason() {
        return reason;
    }

    /**
     * 获取原始错误信息
     *
     * @return 触发降级的原始错误，可能为 null
     */
    public String getOriginalError() {
        return originalError;
    }

    /**
     * 降级模式
     */
    public enum FallbackMode {
        /**
         * 降级为原生 textarea
         * 保持基本编辑功能，丢失富文本特性
         */
        TEXTAREA("textarea"),

        /**
         * 降级为只读模式
         * 内容可见但不可编辑
         */
        READ_ONLY("readonly"),

        /**
         * 显示错误消息
         * 不提供编辑功能
         */
        ERROR_MESSAGE("error"),

        /**
         * 隐藏编辑器
         * 完全不显示
         */
        HIDDEN("hidden");

        private final String jsName;

        /**
         * 预构建的 jsName -> FallbackMode 查找表，O(1) 查找
         */
        private static final java.util.Map<String, FallbackMode> JS_NAME_MAP;
        static {
            java.util.Map<String, FallbackMode> map = new java.util.HashMap<>();
            for (FallbackMode mode : values()) {
                map.put(mode.jsName, mode);
            }
            JS_NAME_MAP = java.util.Collections.unmodifiableMap(map);
        }

        FallbackMode(String jsName) {
            this.jsName = jsName;
        }

        /**
         * 获取 JavaScript 端使用的模式名称
         *
         * @return JS 模式名
         */
        public String getJsName() {
            return jsName;
        }

        /**
         * 从 JS 名称解析模式
         *
         * @param jsName JavaScript 端模式名
         * @return 对应的枚举值，未找到返回 ERROR_MESSAGE
         */
        public static FallbackMode fromJsName(String jsName) {
            FallbackMode mode = JS_NAME_MAP.get(jsName);
            return mode != null ? mode : ERROR_MESSAGE;
        }
    }
}
