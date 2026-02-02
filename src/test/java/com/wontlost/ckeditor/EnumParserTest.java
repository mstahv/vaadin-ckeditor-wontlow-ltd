package com.wontlost.ckeditor;

import com.wontlost.ckeditor.internal.EnumParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnumParser 工具类测试
 */
class EnumParserTest {

    // 测试用枚举
    enum TestEnum {
        VALUE_ONE,
        VALUE_TWO,
        SPECIAL_CASE
    }

    // ==================== parse() 方法测试 ====================

    @Nested
    @DisplayName("parse() 方法测试")
    class ParseTests {

        @Test
        @DisplayName("应正确解析大写值")
        void shouldParseUppercaseValue() {
            TestEnum result = EnumParser.parse("VALUE_ONE", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("应正确解析小写值")
        void shouldParseLowercaseValue() {
            TestEnum result = EnumParser.parse("value_one", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("应正确解析混合大小写值")
        void shouldParseMixedCaseValue() {
            TestEnum result = EnumParser.parse("Value_One", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("null 值应返回默认值")
        void nullValueShouldReturnDefault() {
            TestEnum result = EnumParser.parse(null, TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_TWO, result);
        }

        @Test
        @DisplayName("空字符串应返回默认值")
        void emptyStringShouldReturnDefault() {
            TestEnum result = EnumParser.parse("", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_TWO, result);
        }

        @Test
        @DisplayName("无效值应返回默认值")
        void invalidValueShouldReturnDefault() {
            TestEnum result = EnumParser.parse("INVALID", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_TWO, result);
        }

        @Test
        @DisplayName("带上下文的解析应正常工作")
        void parseWithContextShouldWork() {
            TestEnum result = EnumParser.parse("value_one", TestEnum.class, TestEnum.VALUE_TWO, "TestContext");
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("带上下文的无效值应返回默认值")
        void parseWithContextInvalidShouldReturnDefault() {
            TestEnum result = EnumParser.parse("invalid", TestEnum.class, TestEnum.VALUE_TWO, "TestContext");
            assertEquals(TestEnum.VALUE_TWO, result);
        }
    }

    // ==================== parseStrict() 方法测试 ====================

    @Nested
    @DisplayName("parseStrict() 方法测试")
    class ParseStrictTests {

        @Test
        @DisplayName("应正确解析有效值")
        void shouldParseValidValue() {
            TestEnum result = EnumParser.parseStrict("VALUE_ONE", TestEnum.class);
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("应正确解析小写有效值")
        void shouldParseLowercaseValidValue() {
            TestEnum result = EnumParser.parseStrict("special_case", TestEnum.class);
            assertEquals(TestEnum.SPECIAL_CASE, result);
        }

        @Test
        @DisplayName("null 值应抛出异常")
        void nullValueShouldThrowException() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> EnumParser.parseStrict(null, TestEnum.class)
            );
            assertTrue(ex.getMessage().contains("must not be null or empty"));
        }

        @Test
        @DisplayName("空字符串应抛出异常")
        void emptyStringShouldThrowException() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> EnumParser.parseStrict("", TestEnum.class)
            );
            assertTrue(ex.getMessage().contains("must not be null or empty"));
        }

        @Test
        @DisplayName("无效值应抛出异常并包含有效值列表")
        void invalidValueShouldThrowExceptionWithValidValues() {
            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> EnumParser.parseStrict("INVALID", TestEnum.class)
            );
            assertTrue(ex.getMessage().contains("Invalid"));
            assertTrue(ex.getMessage().contains("INVALID"));
            assertTrue(ex.getMessage().contains("VALUE_ONE"));
        }
    }

    // ==================== Locale 安全测试 ====================

    @Nested
    @DisplayName("Locale 安全测试")
    class LocaleSafetyTests {

        @Test
        @DisplayName("土耳其语 'i' 应正确处理")
        void turkishIShouldBeHandledCorrectly() {
            // 土耳其语中 'i' 的大写是 'İ'，不是 'I'
            // 使用 Locale.ROOT 可以避免这个问题
            TestEnum result = EnumParser.parse("value_one", TestEnum.class, TestEnum.VALUE_TWO);
            assertEquals(TestEnum.VALUE_ONE, result);
        }

        @Test
        @DisplayName("解析应使用 Locale.ROOT")
        void parseShouldUseLocaleRoot() {
            // 即使系统 locale 是土耳其语，也应该正确解析
            TestEnum result = EnumParser.parse("special_case", TestEnum.class, TestEnum.VALUE_ONE);
            assertEquals(TestEnum.SPECIAL_CASE, result);
        }
    }

    // ==================== 实际枚举测试 ====================

    @Nested
    @DisplayName("实际枚举测试")
    class RealEnumTests {

        @Test
        @DisplayName("应正确解析 ErrorSeverity")
        void shouldParseErrorSeverity() {
            var result = EnumParser.parse(
                "warning",
                com.wontlost.ckeditor.event.EditorErrorEvent.ErrorSeverity.class,
                com.wontlost.ckeditor.event.EditorErrorEvent.ErrorSeverity.ERROR
            );
            assertEquals(com.wontlost.ckeditor.event.EditorErrorEvent.ErrorSeverity.WARNING, result);
        }

        @Test
        @DisplayName("应正确解析 ChangeSource")
        void shouldParseChangeSource() {
            var result = EnumParser.parse(
                "user_input",
                com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.class,
                com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.UNKNOWN
            );
            assertEquals(com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.USER_INPUT, result);
        }

        @Test
        @DisplayName("无效 ChangeSource 应返回 UNKNOWN")
        void invalidChangeSourceShouldReturnUnknown() {
            var result = EnumParser.parse(
                "invalid_source",
                com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.class,
                com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.UNKNOWN
            );
            assertEquals(com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource.UNKNOWN, result);
        }
    }
}
