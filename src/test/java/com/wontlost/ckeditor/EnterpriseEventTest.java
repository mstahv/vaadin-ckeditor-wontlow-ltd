package com.wontlost.ckeditor;

import com.wontlost.ckeditor.event.*;
import com.wontlost.ckeditor.event.EditorErrorEvent.EditorError;
import com.wontlost.ckeditor.event.EditorErrorEvent.ErrorSeverity;
import com.wontlost.ckeditor.event.FallbackEvent.FallbackMode;
import com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource;
import com.wontlost.ckeditor.handler.ErrorHandler;
import com.wontlost.ckeditor.handler.HtmlSanitizer;
import com.wontlost.ckeditor.handler.HtmlSanitizer.SanitizationPolicy;
import com.wontlost.ckeditor.handler.UploadHandler;
import com.wontlost.ckeditor.handler.UploadHandler.UploadContext;
import com.wontlost.ckeditor.handler.UploadHandler.UploadResult;
import com.wontlost.ckeditor.handler.UploadHandler.UploadConfig;
import org.jsoup.safety.Safelist;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 企业级事件和处理器 API 综合测试
 */
class EnterpriseEventTest {

    // ==================== EditorError Tests ====================

    @Nested
    @DisplayName("EditorError 测试")
    class EditorErrorTests {

        @Test
        @DisplayName("测试所有错误严重级别")
        void testAllErrorSeverities() {
            assertEquals(3, ErrorSeverity.values().length);
            assertNotNull(ErrorSeverity.valueOf("WARNING"));
            assertNotNull(ErrorSeverity.valueOf("ERROR"));
            assertNotNull(ErrorSeverity.valueOf("FATAL"));
        }

        @Test
        @DisplayName("测试 EditorError 构造和 getter")
        void testEditorErrorConstruction() {
            EditorError error = new EditorError(
                "TEST_ERROR",
                "Test error message",
                ErrorSeverity.ERROR,
                true,
                "stack trace here"
            );

            assertEquals("TEST_ERROR", error.getCode());
            assertEquals("Test error message", error.getMessage());
            assertEquals(ErrorSeverity.ERROR, error.getSeverity());
            assertTrue(error.isRecoverable());
            assertEquals("stack trace here", error.getStackTrace());
        }

        @Test
        @DisplayName("测试 EditorError toString")
        void testEditorErrorToString() {
            EditorError error = new EditorError("CODE", "msg", ErrorSeverity.WARNING, false, null);
            String str = error.toString();
            assertTrue(str.contains("CODE"));
            assertTrue(str.contains("WARNING"));
            assertTrue(str.contains("msg"));
        }

        @Test
        @DisplayName("测试不可恢复的致命错误")
        void testFatalNonRecoverableError() {
            EditorError error = new EditorError(
                "FATAL_ERROR",
                "Critical failure",
                ErrorSeverity.FATAL,
                false,
                "at line 1\nat line 2"
            );

            assertEquals(ErrorSeverity.FATAL, error.getSeverity());
            assertFalse(error.isRecoverable());
            assertNotNull(error.getStackTrace());
        }

        @Test
        @DisplayName("测试 null 堆栈跟踪")
        void testNullStackTrace() {
            EditorError error = new EditorError("E1", "msg", ErrorSeverity.ERROR, true, null);
            assertNull(error.getStackTrace());
        }
    }

    // ==================== FallbackMode Tests ====================

    @Nested
    @DisplayName("FallbackMode 测试")
    class FallbackModeTests {

        @Test
        @DisplayName("测试所有 FallbackMode 值")
        void testAllFallbackModes() {
            assertEquals(4, FallbackMode.values().length);
            assertEquals("textarea", FallbackMode.TEXTAREA.getJsName());
            assertEquals("readonly", FallbackMode.READ_ONLY.getJsName());
            assertEquals("error", FallbackMode.ERROR_MESSAGE.getJsName());
            assertEquals("hidden", FallbackMode.HIDDEN.getJsName());
        }

        @Test
        @DisplayName("测试 fromJsName 正常解析")
        void testFromJsNameValid() {
            assertEquals(FallbackMode.TEXTAREA, FallbackMode.fromJsName("textarea"));
            assertEquals(FallbackMode.READ_ONLY, FallbackMode.fromJsName("readonly"));
            assertEquals(FallbackMode.ERROR_MESSAGE, FallbackMode.fromJsName("error"));
            assertEquals(FallbackMode.HIDDEN, FallbackMode.fromJsName("hidden"));
        }

        @Test
        @DisplayName("测试 fromJsName 未知值默认返回 ERROR_MESSAGE")
        void testFromJsNameUnknown() {
            assertEquals(FallbackMode.ERROR_MESSAGE, FallbackMode.fromJsName("unknown"));
            assertEquals(FallbackMode.ERROR_MESSAGE, FallbackMode.fromJsName(""));
            assertEquals(FallbackMode.ERROR_MESSAGE, FallbackMode.fromJsName(null));
        }
    }

    // ==================== ChangeSource Tests ====================

    @Nested
    @DisplayName("ChangeSource 测试")
    class ChangeSourceTests {

        @Test
        @DisplayName("测试所有 ChangeSource 值")
        void testAllChangeSources() {
            assertEquals(6, ChangeSource.values().length);
            assertNotNull(ChangeSource.valueOf("USER_INPUT"));
            assertNotNull(ChangeSource.valueOf("API"));
            assertNotNull(ChangeSource.valueOf("UNDO_REDO"));
            assertNotNull(ChangeSource.valueOf("PASTE"));
            assertNotNull(ChangeSource.valueOf("COLLABORATION"));
            assertNotNull(ChangeSource.valueOf("UNKNOWN"));
        }
    }

    // ==================== ErrorHandler Tests ====================

    @Nested
    @DisplayName("ErrorHandler 测试")
    class ErrorHandlerTests {

        @Test
        @DisplayName("测试日志处理器处理所有严重级别")
        void testLoggingHandlerAllSeverities() {
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger("test");
            ErrorHandler handler = ErrorHandler.logging(logger);

            EditorError warning = new EditorError("W1", "warning", ErrorSeverity.WARNING, true, null);
            assertFalse(handler.handleError(warning));

            EditorError error = new EditorError("E1", "error", ErrorSeverity.ERROR, false, null);
            assertFalse(handler.handleError(error));

            EditorError fatal = new EditorError("F1", "fatal", ErrorSeverity.FATAL, false, "trace");
            assertFalse(handler.handleError(fatal));
        }

        @Test
        @DisplayName("测试处理器组合 - 第一个处理")
        void testComposeFirstHandles() {
            ErrorHandler handler1 = error -> true; // 处理所有
            ErrorHandler handler2 = error -> {
                fail("不应该到达第二个处理器");
                return false;
            };
            ErrorHandler composed = ErrorHandler.compose(handler1, handler2);

            EditorError error = new EditorError("E1", "msg", ErrorSeverity.ERROR, true, null);
            assertTrue(composed.handleError(error));
        }

        @Test
        @DisplayName("测试处理器组合 - 传递到第二个")
        void testComposeSecondHandles() {
            AtomicBoolean firstCalled = new AtomicBoolean(false);
            AtomicBoolean secondCalled = new AtomicBoolean(false);

            ErrorHandler handler1 = error -> {
                firstCalled.set(true);
                return false;
            };
            ErrorHandler handler2 = error -> {
                secondCalled.set(true);
                return error.getCode().equals("HANDLED");
            };
            ErrorHandler composed = ErrorHandler.compose(handler1, handler2);

            EditorError handled = new EditorError("HANDLED", "msg", ErrorSeverity.ERROR, true, null);
            assertTrue(composed.handleError(handled));
            assertTrue(firstCalled.get());
            assertTrue(secondCalled.get());
        }

        @Test
        @DisplayName("测试处理器组合 - 都不处理")
        void testComposeNoneHandles() {
            ErrorHandler handler1 = error -> false;
            ErrorHandler handler2 = error -> false;
            ErrorHandler composed = ErrorHandler.compose(handler1, handler2);

            EditorError error = new EditorError("E1", "msg", ErrorSeverity.ERROR, true, null);
            assertFalse(composed.handleError(error));
        }

        @Test
        @DisplayName("测试空处理器数组")
        void testComposeEmpty() {
            ErrorHandler composed = ErrorHandler.compose();
            EditorError error = new EditorError("E1", "msg", ErrorSeverity.ERROR, true, null);
            assertFalse(composed.handleError(error));
        }

        @Test
        @DisplayName("测试函数式接口 lambda")
        void testFunctionalInterface() {
            AtomicInteger callCount = new AtomicInteger(0);
            ErrorHandler handler = error -> {
                callCount.incrementAndGet();
                return error.getSeverity() == ErrorSeverity.WARNING;
            };

            EditorError warning = new EditorError("W1", "msg", ErrorSeverity.WARNING, true, null);
            assertTrue(handler.handleError(warning));
            assertEquals(1, callCount.get());

            EditorError error = new EditorError("E1", "msg", ErrorSeverity.ERROR, true, null);
            assertFalse(handler.handleError(error));
            assertEquals(2, callCount.get());
        }
    }

    // ==================== HtmlSanitizer Tests ====================

    @Nested
    @DisplayName("HtmlSanitizer 测试")
    class HtmlSanitizerTests {

        @Test
        @DisplayName("测试 NONE 策略 - 不清理")
        void testNonePolicy() {
            HtmlSanitizer none = HtmlSanitizer.withPolicy(SanitizationPolicy.NONE);
            String html = "<script>alert('xss')</script><p>text</p>";
            assertEquals(html, none.sanitize(html));
        }

        @Test
        @DisplayName("测试 BASIC 策略 - 移除脚本")
        void testBasicPolicy() {
            HtmlSanitizer basic = HtmlSanitizer.withPolicy(SanitizationPolicy.BASIC);
            String html = "<script>alert('xss')</script><p>text</p>";
            String result = basic.sanitize(html);
            assertFalse(result.contains("<script>"));
            assertTrue(result.contains("text"));
        }

        @Test
        @DisplayName("测试 RELAXED 策略")
        void testRelaxedPolicy() {
            HtmlSanitizer relaxed = HtmlSanitizer.withPolicy(SanitizationPolicy.RELAXED);
            String html = "<table><tr><td>cell</td></tr></table><script>bad</script>";
            String result = relaxed.sanitize(html);
            assertTrue(result.contains("<table>"));
            assertFalse(result.contains("<script>"));
        }

        @Test
        @DisplayName("测试 STRICT 策略 - 只保留基本格式")
        void testStrictPolicy() {
            HtmlSanitizer strict = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);

            // 保留基本格式化标签
            String basic = "<p>paragraph</p><b>bold</b><i>italic</i>";
            String result = strict.sanitize(basic);
            assertTrue(result.contains("<p>"));
            assertTrue(result.contains("<b>"));
            assertTrue(result.contains("<i>"));

            // 移除 div 和属性
            String withDiv = "<div class='test'><p>text</p></div>";
            result = strict.sanitize(withDiv);
            assertTrue(result.contains("<p>"));
            assertFalse(result.contains("<div"));
            assertFalse(result.contains("class"));

            // 保留标题
            String heading = "<h1>Title</h1><h2>Subtitle</h2>";
            result = strict.sanitize(heading);
            assertTrue(result.contains("<h1>"));
            assertTrue(result.contains("<h2>"));

            // 保留列表
            String list = "<ul><li>item</li></ul><ol><li>numbered</li></ol>";
            result = strict.sanitize(list);
            assertTrue(result.contains("<ul>"));
            assertTrue(result.contains("<ol>"));
            assertTrue(result.contains("<li>"));
        }

        @Test
        @DisplayName("测试空输入处理")
        void testEmptyInput() {
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);
            assertEquals("", sanitizer.sanitize(null));
            assertEquals("", sanitizer.sanitize(""));
        }

        @Test
        @DisplayName("测试清理器链式调用")
        void testChaining() {
            HtmlSanitizer first = html -> html.replace("a", "b");
            HtmlSanitizer second = html -> html.toUpperCase();
            HtmlSanitizer chained = first.andThen(second);

            assertEquals("BBCD", chained.sanitize("abcd"));
        }

        @Test
        @DisplayName("测试 passthrough 清理器")
        void testPassthrough() {
            HtmlSanitizer passthrough = HtmlSanitizer.passthrough();
            String html = "<script>test</script><div onclick='bad'>text</div>";
            assertEquals(html, passthrough.sanitize(html));
        }

        @Test
        @DisplayName("测试自定义 Safelist")
        void testCustomSafelist() {
            Safelist custom = new Safelist().addTags("custom", "special");
            HtmlSanitizer sanitizer = HtmlSanitizer.withSafelist(custom);

            String html = "<custom>allowed</custom><p>removed</p>";
            String result = sanitizer.sanitize(html);
            assertTrue(result.contains("<custom>"));
            assertFalse(result.contains("<p>"));
        }

        @Test
        @DisplayName("测试多次链式调用")
        void testMultipleChaining() {
            HtmlSanitizer s1 = html -> html + "1";
            HtmlSanitizer s2 = html -> html + "2";
            HtmlSanitizer s3 = html -> html + "3";

            HtmlSanitizer chained = s1.andThen(s2).andThen(s3);
            assertEquals("x123", chained.sanitize("x"));
        }
    }

    // ==================== UploadHandler Tests ====================

    @Nested
    @DisplayName("UploadHandler 测试")
    class UploadHandlerTests {

        @Test
        @DisplayName("测试 UploadContext 构造")
        void testUploadContext() {
            UploadContext context = new UploadContext("test.jpg", "image/jpeg", 1024);

            assertEquals("test.jpg", context.getFileName());
            assertEquals("image/jpeg", context.getMimeType());
            assertEquals(1024, context.getFileSize());
            assertTrue(context.isImage());
        }

        @Test
        @DisplayName("测试 isImage 方法")
        void testIsImage() {
            assertTrue(new UploadContext("a.jpg", "image/jpeg", 100).isImage());
            assertTrue(new UploadContext("b.png", "image/png", 100).isImage());
            assertTrue(new UploadContext("c.gif", "image/gif", 100).isImage());
            assertTrue(new UploadContext("d.webp", "image/webp", 100).isImage());

            assertFalse(new UploadContext("e.pdf", "application/pdf", 100).isImage());
            assertFalse(new UploadContext("f.txt", "text/plain", 100).isImage());
            assertFalse(new UploadContext("g.doc", null, 100).isImage());
        }

        @Test
        @DisplayName("测试 UploadResult 成功")
        void testUploadResultSuccess() {
            UploadResult result = new UploadResult("/uploads/image.jpg");

            assertTrue(result.isSuccess());
            assertEquals("/uploads/image.jpg", result.getUrl());
            assertNull(result.getErrorMessage());
        }

        @Test
        @DisplayName("测试 UploadResult 失败")
        void testUploadResultFailure() {
            UploadResult result = UploadResult.failure("File too large");

            assertFalse(result.isSuccess());
            assertNull(result.getUrl());
            assertEquals("File too large", result.getErrorMessage());
        }

        @Test
        @DisplayName("测试 UploadConfig 默认值")
        void testUploadConfigDefaults() {
            UploadConfig config = new UploadConfig();

            assertEquals(10 * 1024 * 1024, config.getMaxFileSize());
            assertArrayEquals(
                new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"},
                config.getAllowedMimeTypes()
            );
        }

        @Test
        @DisplayName("测试 UploadConfig 链式设置")
        void testUploadConfigChaining() {
            UploadConfig config = new UploadConfig()
                .setMaxFileSize(5 * 1024 * 1024)
                .setAllowedMimeTypes("image/jpeg", "image/png");

            assertEquals(5 * 1024 * 1024, config.getMaxFileSize());
            assertArrayEquals(new String[]{"image/jpeg", "image/png"}, config.getAllowedMimeTypes());
        }

        @Test
        @DisplayName("测试 UploadConfig 验证 - 通过")
        void testUploadConfigValidatePass() {
            UploadConfig config = new UploadConfig();
            UploadContext context = new UploadContext("test.jpg", "image/jpeg", 1024);

            assertNull(config.validate(context));
        }

        @Test
        @DisplayName("测试 UploadConfig 验证 - 文件过大")
        void testUploadConfigValidateFileTooLarge() {
            UploadConfig config = new UploadConfig().setMaxFileSize(100);
            UploadContext context = new UploadContext("test.jpg", "image/jpeg", 1024);

            String error = config.validate(context);
            assertNotNull(error);
            assertTrue(error.contains("1024"));
            assertTrue(error.contains("100"));
        }

        @Test
        @DisplayName("测试 UploadConfig 验证 - MIME 类型不允许")
        void testUploadConfigValidateMimeNotAllowed() {
            UploadConfig config = new UploadConfig();
            UploadContext context = new UploadContext("test.pdf", "application/pdf", 1024);

            String error = config.validate(context);
            assertNotNull(error);
            assertTrue(error.contains("application/pdf"));
        }

        @Test
        @DisplayName("测试 UploadHandler 函数式接口")
        void testUploadHandlerFunctional() {
            UploadHandler handler = (context, stream) ->
                CompletableFuture.completedFuture(new UploadResult("/uploaded/" + context.getFileName()));

            UploadContext context = new UploadContext("image.png", "image/png", 500);
            ByteArrayInputStream stream = new ByteArrayInputStream(new byte[500]);

            CompletableFuture<UploadResult> future = handler.handleUpload(context, stream);
            UploadResult result = future.join();

            assertTrue(result.isSuccess());
            assertEquals("/uploaded/image.png", result.getUrl());
        }

        @Test
        @DisplayName("测试 UploadHandler 异步失败")
        void testUploadHandlerAsyncFailure() {
            UploadHandler handler = (context, stream) ->
                CompletableFuture.completedFuture(UploadResult.failure("Upload failed"));

            UploadContext context = new UploadContext("test.jpg", "image/jpeg", 100);
            CompletableFuture<UploadResult> future = handler.handleUpload(context, null);
            UploadResult result = future.join();

            assertFalse(result.isSuccess());
            assertEquals("Upload failed", result.getErrorMessage());
        }
    }

    // ==================== VaadinCKEditor Builder Tests ====================

    @Nested
    @DisplayName("VaadinCKEditor Builder 企业级选项测试")
    class BuilderEnterpriseTests {

        @Test
        @DisplayName("测试 withFallbackMode")
        void testWithFallbackMode() {
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withFallbackMode(FallbackMode.READ_ONLY)
                .build();

            assertEquals(FallbackMode.READ_ONLY, editor.getFallbackMode());
        }

        @Test
        @DisplayName("测试 withErrorHandler")
        void testWithErrorHandler() {
            ErrorHandler handler = error -> true;
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withErrorHandler(handler)
                .build();

            assertSame(handler, editor.getErrorHandler());
        }

        @Test
        @DisplayName("测试 withHtmlSanitizer")
        void testWithHtmlSanitizer() {
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withHtmlSanitizer(sanitizer)
                .build();

            assertSame(sanitizer, editor.getHtmlSanitizer());
        }

        @Test
        @DisplayName("测试 withUploadHandler")
        void testWithUploadHandler() {
            UploadHandler handler = (ctx, stream) -> CompletableFuture.completedFuture(new UploadResult("/test"));
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withUploadHandler(handler)
                .build();

            assertSame(handler, editor.getUploadHandler());
        }

        @Test
        @DisplayName("测试组合所有企业级选项")
        void testAllEnterpriseOptions() {
            ErrorHandler errorHandler = error -> false;
            HtmlSanitizer sanitizer = HtmlSanitizer.passthrough();
            UploadHandler uploadHandler = (ctx, stream) -> CompletableFuture.completedFuture(new UploadResult("/url"));

            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withFallbackMode(FallbackMode.HIDDEN)
                .withErrorHandler(errorHandler)
                .withHtmlSanitizer(sanitizer)
                .withUploadHandler(uploadHandler)
                .build();

            assertEquals(FallbackMode.HIDDEN, editor.getFallbackMode());
            assertSame(errorHandler, editor.getErrorHandler());
            assertSame(sanitizer, editor.getHtmlSanitizer());
            assertSame(uploadHandler, editor.getUploadHandler());
        }

        @Test
        @DisplayName("测试 setter 方法")
        void testSetters() {
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .build();

            // 测试 setter
            ErrorHandler handler = error -> true;
            editor.setErrorHandler(handler);
            assertSame(handler, editor.getErrorHandler());

            HtmlSanitizer sanitizer = HtmlSanitizer.passthrough();
            editor.setHtmlSanitizer(sanitizer);
            assertSame(sanitizer, editor.getHtmlSanitizer());

            UploadHandler uploadHandler = (ctx, stream) -> CompletableFuture.completedFuture(new UploadResult("/"));
            editor.setUploadHandler(uploadHandler);
            assertSame(uploadHandler, editor.getUploadHandler());

            editor.setFallbackMode(FallbackMode.ERROR_MESSAGE);
            assertEquals(FallbackMode.ERROR_MESSAGE, editor.getFallbackMode());
        }

        @Test
        @DisplayName("测试默认值")
        void testDefaultValues() {
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .build();

            assertEquals(FallbackMode.TEXTAREA, editor.getFallbackMode());
            assertNull(editor.getErrorHandler());
            assertNull(editor.getHtmlSanitizer());
            assertNull(editor.getUploadHandler());
        }
    }

    // ==================== 测试辅助方法 ====================

    /**
     * 创建用于测试的 VaadinCKEditor 实例
     */
    private static VaadinCKEditor createTestEditor() {
        return VaadinCKEditor.create()
            .withPreset(CKEditorPreset.BASIC)
            .build();
    }

    // ==================== Safe Enum Parsing Tests ====================

    @Nested
    @DisplayName("安全枚举解析测试")
    class SafeEnumParsingTests {

        @Test
        @DisplayName("测试 ErrorSeverity 大小写不敏感")
        void testErrorSeverityCaseInsensitive() {
            // 测试各种大小写组合
            assertEquals(ErrorSeverity.WARNING, ErrorSeverity.valueOf("WARNING"));
            assertEquals(ErrorSeverity.ERROR, ErrorSeverity.valueOf("ERROR"));
            assertEquals(ErrorSeverity.FATAL, ErrorSeverity.valueOf("FATAL"));
        }

        @Test
        @DisplayName("测试 ChangeSource 所有有效值")
        void testChangeSourceAllValues() {
            assertEquals(ChangeSource.USER_INPUT, ChangeSource.valueOf("USER_INPUT"));
            assertEquals(ChangeSource.API, ChangeSource.valueOf("API"));
            assertEquals(ChangeSource.UNDO_REDO, ChangeSource.valueOf("UNDO_REDO"));
            assertEquals(ChangeSource.PASTE, ChangeSource.valueOf("PASTE"));
            assertEquals(ChangeSource.COLLABORATION, ChangeSource.valueOf("COLLABORATION"));
            assertEquals(ChangeSource.UNKNOWN, ChangeSource.valueOf("UNKNOWN"));
        }

        @Test
        @DisplayName("测试无效枚举值抛出异常")
        void testInvalidEnumThrows() {
            assertThrows(IllegalArgumentException.class, () -> ErrorSeverity.valueOf("INVALID"));
            assertThrows(IllegalArgumentException.class, () -> ChangeSource.valueOf("INVALID"));
        }
    }

    // ==================== HtmlSanitizer Integration Tests ====================

    @Nested
    @DisplayName("HtmlSanitizer 集成测试")
    class HtmlSanitizerIntegrationTests {

        @Test
        @DisplayName("测试 getSanitizedValue 无 sanitizer")
        void testGetSanitizedValueWithoutSanitizer() {
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withValue("<script>alert('xss')</script><p>text</p>")
                .build();

            // 没有设置 sanitizer，返回原始值
            assertEquals("<script>alert('xss')</script><p>text</p>", editor.getSanitizedValue());
        }

        @Test
        @DisplayName("测试 getSanitizedValue 有 sanitizer")
        void testGetSanitizedValueWithSanitizer() {
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.BASIC);
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withValue("<script>alert('xss')</script><p>text</p>")
                .withHtmlSanitizer(sanitizer)
                .build();

            String sanitized = editor.getSanitizedValue();
            assertFalse(sanitized.contains("<script>"));
            assertTrue(sanitized.contains("text"));
        }

        @Test
        @DisplayName("测试 getSanitizedValue 空值")
        void testGetSanitizedValueNull() {
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withHtmlSanitizer(sanitizer)
                .build();

            // 空值应返回空字符串或 null
            String result = editor.getSanitizedValue();
            assertTrue(result == null || result.isEmpty());
        }

        @Test
        @DisplayName("测试 STRICT 策略移除危险标签")
        void testStrictPolicyRemovesDangerousTags() {
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withValue("<div onclick='bad'><p>safe</p><iframe src='evil'></iframe></div>")
                .withHtmlSanitizer(sanitizer)
                .build();

            String sanitized = editor.getSanitizedValue();
            assertFalse(sanitized.contains("onclick"));
            assertFalse(sanitized.contains("<iframe"));
            assertFalse(sanitized.contains("<div"));
            assertTrue(sanitized.contains("<p>"));
            assertTrue(sanitized.contains("safe"));
        }
    }

    // ==================== AutosaveEvent Tests ====================

    @Nested
    @DisplayName("AutosaveEvent 测试")
    class AutosaveEventTests {

        @Test
        @DisplayName("测试 AutosaveEvent 成功场景")
        void testAutosaveEventSuccess() {
            VaadinCKEditor editor = createTestEditor();
            AutosaveEvent event = new AutosaveEvent(editor, false, "<p>content</p>");

            assertTrue(event.isSuccess());
            assertEquals("<p>content</p>", event.getContent());
            assertNull(event.getErrorMessage());
            assertTrue(event.getTimestamp() > 0);
        }

        @Test
        @DisplayName("测试 AutosaveEvent 失败场景")
        void testAutosaveEventFailure() {
            VaadinCKEditor editor = createTestEditor();
            AutosaveEvent event = new AutosaveEvent(editor, false, "<p>content</p>", false, "Save failed");

            assertFalse(event.isSuccess());
            assertEquals("<p>content</p>", event.getContent());
            assertEquals("Save failed", event.getErrorMessage());
        }

        @Test
        @DisplayName("测试 AutosaveEvent 时间戳")
        void testAutosaveEventTimestamp() {
            VaadinCKEditor editor = createTestEditor();
            long before = System.currentTimeMillis();
            AutosaveEvent event = new AutosaveEvent(editor, false, "content");
            long after = System.currentTimeMillis();

            assertTrue(event.getTimestamp() >= before);
            assertTrue(event.getTimestamp() <= after);
        }
    }

    // ==================== ContentChangeEvent Tests ====================

    @Nested
    @DisplayName("ContentChangeEvent 测试")
    class ContentChangeEventTests {

        @Test
        @DisplayName("测试 hasChanged 方法")
        void testHasChanged() {
            VaadinCKEditor editor = createTestEditor();

            ContentChangeEvent changed = new ContentChangeEvent(editor, false, "old", "new", ChangeSource.USER_INPUT);
            assertTrue(changed.hasChanged());

            ContentChangeEvent notChanged = new ContentChangeEvent(editor, false, "same", "same", ChangeSource.USER_INPUT);
            assertFalse(notChanged.hasChanged());

            ContentChangeEvent fromNull = new ContentChangeEvent(editor, false, null, "new", ChangeSource.API);
            assertTrue(fromNull.hasChanged());

            ContentChangeEvent toNull = new ContentChangeEvent(editor, false, "old", null, ChangeSource.API);
            assertTrue(toNull.hasChanged());

            ContentChangeEvent bothNull = new ContentChangeEvent(editor, false, null, null, ChangeSource.UNKNOWN);
            assertFalse(bothNull.hasChanged());
        }

        @Test
        @DisplayName("测试 getLengthDelta 方法")
        void testGetLengthDelta() {
            VaadinCKEditor editor = createTestEditor();

            ContentChangeEvent added = new ContentChangeEvent(editor, false, "ab", "abcd", ChangeSource.USER_INPUT);
            assertEquals(2, added.getLengthDelta());

            ContentChangeEvent removed = new ContentChangeEvent(editor, false, "abcd", "ab", ChangeSource.UNDO_REDO);
            assertEquals(-2, removed.getLengthDelta());

            ContentChangeEvent noChange = new ContentChangeEvent(editor, false, "same", "same", ChangeSource.PASTE);
            assertEquals(0, noChange.getLengthDelta());

            ContentChangeEvent fromNull = new ContentChangeEvent(editor, false, null, "new", ChangeSource.API);
            assertEquals(3, fromNull.getLengthDelta());

            ContentChangeEvent toNull = new ContentChangeEvent(editor, false, "old", null, ChangeSource.COLLABORATION);
            assertEquals(-3, toNull.getLengthDelta());
        }

        @Test
        @DisplayName("测试所有 ChangeSource 值")
        void testAllChangeSources() {
            VaadinCKEditor editor = createTestEditor();
            for (ChangeSource source : ChangeSource.values()) {
                ContentChangeEvent event = new ContentChangeEvent(editor, false, "a", "b", source);
                assertEquals(source, event.getChangeSource());
            }
        }
    }

    // ==================== Collaborative Preset Tests ====================

    @Nested
    @DisplayName("COLLABORATIVE 预设测试")
    class CollaborativePresetTests {

        @Test
        @DisplayName("测试 COLLABORATIVE 预设存在")
        void testCollaborativePresetExists() {
            assertNotNull(CKEditorPreset.COLLABORATIVE);
            assertEquals("Collaborative Editor", CKEditorPreset.COLLABORATIVE.getDisplayName());
        }

        @Test
        @DisplayName("测试 COLLABORATIVE 预设包含基础插件")
        void testCollaborativePresetHasBasePlugins() {
            CKEditorPreset preset = CKEditorPreset.COLLABORATIVE;

            // Core plugins
            assertTrue(preset.hasPlugin(CKEditorPlugin.ESSENTIALS));
            assertTrue(preset.hasPlugin(CKEditorPlugin.PARAGRAPH));
            assertTrue(preset.hasPlugin(CKEditorPlugin.UNDO));

            // Document features
            assertTrue(preset.hasPlugin(CKEditorPlugin.AUTOSAVE));
            assertTrue(preset.hasPlugin(CKEditorPlugin.WORD_COUNT));

            // Formatting
            assertTrue(preset.hasPlugin(CKEditorPlugin.BOLD));
            assertTrue(preset.hasPlugin(CKEditorPlugin.ITALIC));
            assertTrue(preset.hasPlugin(CKEditorPlugin.HEADING));
        }

        @Test
        @DisplayName("测试 COLLABORATIVE 预设有工具栏")
        void testCollaborativePresetHasToolbar() {
            String[] toolbar = CKEditorPreset.COLLABORATIVE.getDefaultToolbar();
            assertNotNull(toolbar);
            assertTrue(toolbar.length > 0);
        }

        @Test
        @DisplayName("测试 COLLABORATIVE 预设估计大小")
        void testCollaborativePresetSize() {
            int size = CKEditorPreset.COLLABORATIVE.getEstimatedSize();
            assertEquals(850, size);
        }

        @Test
        @DisplayName("测试使用 COLLABORATIVE 预设创建编辑器")
        void testCreateEditorWithCollaborativePreset() {
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.COLLABORATIVE)
                .build();

            assertNotNull(editor);
        }

        @Test
        @DisplayName("测试 COLLABORATIVE 预设添加 Premium 插件")
        void testCollaborativeWithPremiumPlugins() {
            // 测试可以添加 Premium 协作插件
            VaadinCKEditor editor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.COLLABORATIVE)
                .withLicenseKey("test-license-key")
                .addCustomPlugin(CustomPlugin.fromPremium("Comments"))
                .addCustomPlugin(CustomPlugin.fromPremium("TrackChanges"))
                .build();

            assertNotNull(editor);
        }
    }
}
