package com.wontlost.ckeditor.internal;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 安全的枚举解析工具类。
 *
 * <p>提供类型安全的枚举解析，避免以下常见问题：</p>
 * <ul>
 *   <li>土耳其语等特殊 locale 导致的大小写转换问题</li>
 *   <li>null 或空字符串导致的异常</li>
 *   <li>无效值导致的 IllegalArgumentException</li>
 * </ul>
 *
 * <p>此类是内部 API，不应直接由外部代码使用。</p>
 */
public final class EnumParser {

    private static final Logger logger = Logger.getLogger(EnumParser.class.getName());

    private EnumParser() {
        // 工具类，禁止实例化
    }

    /**
     * 安全解析枚举值。
     *
     * <p>解析规则：</p>
     * <ul>
     *   <li>使用 {@link Locale#ROOT} 进行大小写转换，确保国际化安全</li>
     *   <li>null 或空字符串返回默认值</li>
     *   <li>无效值返回默认值并记录警告日志</li>
     * </ul>
     *
     * @param value 要解析的字符串值
     * @param enumType 目标枚举类型
     * @param defaultValue 解析失败时的默认值
     * @param <T> 枚举类型
     * @return 解析后的枚举值，失败时返回默认值
     */
    public static <T extends Enum<T>> T parse(String value, Class<T> enumType, T defaultValue) {
        if (value == null || value.isEmpty()) {
            logger.log(Level.FINE, () ->
                String.format("Null or empty value for %s, using default: %s",
                    enumType.getSimpleName(), defaultValue));
            return defaultValue;
        }

        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, () ->
                String.format("Invalid %s value: '%s', using default: %s",
                    enumType.getSimpleName(), value, defaultValue));
            return defaultValue;
        }
    }

    /**
     * 安全解析枚举值，支持自定义日志消息。
     *
     * @param value 要解析的字符串值
     * @param enumType 目标枚举类型
     * @param defaultValue 解析失败时的默认值
     * @param context 上下文描述（用于日志消息）
     * @param <T> 枚举类型
     * @return 解析后的枚举值，失败时返回默认值
     */
    public static <T extends Enum<T>> T parse(String value, Class<T> enumType, T defaultValue, String context) {
        if (value == null || value.isEmpty()) {
            logger.log(Level.FINE, () ->
                String.format("[%s] Null or empty value for %s, using default: %s",
                    context, enumType.getSimpleName(), defaultValue));
            return defaultValue;
        }

        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, () ->
                String.format("[%s] Invalid %s value: '%s', using default: %s",
                    context, enumType.getSimpleName(), value, defaultValue));
            return defaultValue;
        }
    }

    /**
     * 严格解析枚举值，无效值抛出异常。
     *
     * @param value 要解析的字符串值
     * @param enumType 目标枚举类型
     * @param <T> 枚举类型
     * @return 解析后的枚举值
     * @throws IllegalArgumentException 如果值为 null、空或无效
     */
    public static <T extends Enum<T>> T parseStrict(String value, Class<T> enumType) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                String.format("%s value must not be null or empty", enumType.getSimpleName()));
        }

        try {
            return Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Invalid %s value: '%s'. Valid values: %s",
                    enumType.getSimpleName(), value, java.util.Arrays.toString(enumType.getEnumConstants())),
                e);
        }
    }
}
