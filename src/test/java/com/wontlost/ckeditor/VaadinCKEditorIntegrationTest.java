package com.wontlost.ckeditor;

import com.wontlost.ckeditor.event.*;
import com.wontlost.ckeditor.event.ContentChangeEvent.ChangeSource;
import com.wontlost.ckeditor.event.EditorErrorEvent.EditorError;
import com.wontlost.ckeditor.event.EditorErrorEvent.ErrorSeverity;
import com.wontlost.ckeditor.event.FallbackEvent.FallbackMode;
import com.wontlost.ckeditor.handler.ErrorHandler;
import com.wontlost.ckeditor.handler.HtmlSanitizer;
import com.wontlost.ckeditor.handler.HtmlSanitizer.SanitizationPolicy;
import com.wontlost.ckeditor.internal.EventDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VaadinCKEditor 集成测试。
 * 测试组件的完整生命周期、事件分发、状态管理等。
 */
class VaadinCKEditorIntegrationTest {

    private VaadinCKEditor editor;

    @BeforeEach
    void setUp() {
        editor = VaadinCKEditor.create()
            .withPreset(CKEditorPreset.BASIC)
            .build();
    }

    // ==================== 组件生命周期测试 ====================

    @Nested
    @DisplayName("组件生命周期测试")
    class LifecycleTests {

        @Test
        @DisplayName("创建编辑器后应有正确的初始状态")
        void testInitialState() {
            VaadinCKEditor newEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .build();

            assertNotNull(newEditor);
            assertEquals("", newEditor.getValue());
            assertFalse(newEditor.isReadOnly());
            assertEquals(FallbackMode.TEXTAREA, newEditor.getFallbackMode());
            assertNull(newEditor.getErrorHandler());
            assertNull(newEditor.getHtmlSanitizer());
            assertNull(newEditor.getUploadHandler());
        }

        @Test
        @DisplayName("设置初始值后应正确返回")
        void testInitialValue() {
            VaadinCKEditor newEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withValue("<p>Hello World</p>")
                .build();

            assertEquals("<p>Hello World</p>", newEditor.getValue());
        }

        @Test
        @DisplayName("清理监听器后统计应为零")
        void testCleanupListeners() {
            editor.addEditorReadyListener(event -> {});
            editor.addEditorErrorListener(event -> {});
            editor.addAutosaveListener(event -> {});

            EventDispatcher.ListenerStats statsBefore = editor.getListenerStats();
            assertEquals(3, statsBefore.total());

            editor.cleanupListeners();

            EventDispatcher.ListenerStats statsAfter = editor.getListenerStats();
            assertEquals(0, statsAfter.total());
        }
    }

    // ==================== 事件监听器注册测试 ====================

    @Nested
    @DisplayName("事件监听器注册测试")
    class ListenerRegistrationTests {

        @Test
        @DisplayName("注册监听器后统计应正确")
        void testListenerStats() {
            assertEquals(0, editor.getListenerStats().total());

            editor.addEditorReadyListener(event -> {});
            assertEquals(1, editor.getListenerStats().ready);
            assertEquals(1, editor.getListenerStats().total());

            editor.addEditorErrorListener(event -> {});
            assertEquals(1, editor.getListenerStats().error);
            assertEquals(2, editor.getListenerStats().total());

            editor.addAutosaveListener(event -> {});
            assertEquals(1, editor.getListenerStats().autosave);
            assertEquals(3, editor.getListenerStats().total());

            editor.addContentChangeListener(event -> {});
            assertEquals(1, editor.getListenerStats().contentChange);
            assertEquals(4, editor.getListenerStats().total());

            editor.addFallbackListener(event -> {});
            assertEquals(1, editor.getListenerStats().fallback);
            assertEquals(5, editor.getListenerStats().total());
        }

        @Test
        @DisplayName("移除监听器后统计应减少")
        void testListenerRemoval() {
            var reg1 = editor.addEditorReadyListener(event -> {});
            var reg2 = editor.addEditorErrorListener(event -> {});

            assertEquals(2, editor.getListenerStats().total());

            reg1.remove();
            assertEquals(1, editor.getListenerStats().total());
            assertEquals(0, editor.getListenerStats().ready);
            assertEquals(1, editor.getListenerStats().error);

            reg2.remove();
            assertEquals(0, editor.getListenerStats().total());
        }

        @Test
        @DisplayName("多次注册同类型监听器应累加")
        void testMultipleListeners() {
            editor.addEditorReadyListener(event -> {});
            editor.addEditorReadyListener(event -> {});
            editor.addEditorReadyListener(event -> {});

            assertEquals(3, editor.getListenerStats().ready);
            assertEquals(3, editor.getListenerStats().total());
        }
    }

    // ==================== 值操作测试 ====================

    @Nested
    @DisplayName("值操作测试")
    class ValueOperationsTests {

        @Test
        @DisplayName("setValue 应更新值")
        void testSetValue() {
            editor.setValue("<p>New content</p>");
            assertEquals("<p>New content</p>", editor.getValue());
        }

        @Test
        @DisplayName("setValue null 应转为空字符串")
        void testSetValueNull() {
            editor.setValue("<p>content</p>");
            editor.setValue(null);
            assertEquals("", editor.getValue());
        }

        @Test
        @DisplayName("clear 应清空内容")
        void testClear() {
            editor.setValue("<p>Some content</p>");
            editor.clear();
            assertEquals("", editor.getValue());
        }

        @Test
        @DisplayName("getPlainText 应提取纯文本")
        void testGetPlainText() {
            editor.setValue("<p>Hello <b>World</b></p>");
            assertEquals("Hello World", editor.getPlainText());
        }

        @Test
        @DisplayName("getPlainText 空值应返回空字符串")
        void testGetPlainTextEmpty() {
            editor.setValue("");
            assertEquals("", editor.getPlainText());
        }

        @Test
        @DisplayName("getSanitizedHtml 应清理危险标签")
        void testGetSanitizedHtml() {
            editor.setValue("<p>Text</p><script>alert('xss')</script>");
            String sanitized = editor.getSanitizedHtml();
            assertTrue(sanitized.contains("Text"));
            assertFalse(sanitized.contains("<script>"));
        }
    }

    // ==================== 内容统计测试 ====================

    @Nested
    @DisplayName("内容统计测试")
    class ContentStatsTests {

        @Test
        @DisplayName("getCharacterCount 应返回正确字符数")
        void testCharacterCount() {
            editor.setValue("<p>Hello</p>");
            assertEquals(5, editor.getCharacterCount());

            editor.setValue("<p>Hello World</p>");
            assertEquals(11, editor.getCharacterCount());
        }

        @Test
        @DisplayName("getWordCount 应返回正确单词数")
        void testWordCount() {
            editor.setValue("<p>Hello World</p>");
            assertEquals(2, editor.getWordCount());

            editor.setValue("<p>One two three four five</p>");
            assertEquals(5, editor.getWordCount());
        }

        @Test
        @DisplayName("getWordCount 空内容应返回零")
        void testWordCountEmpty() {
            editor.setValue("");
            assertEquals(0, editor.getWordCount());
        }

        @Test
        @DisplayName("isContentEmpty 应正确检测空内容")
        void testIsContentEmpty() {
            editor.setValue("");
            assertTrue(editor.isContentEmpty());

            editor.setValue("<p></p>");
            assertTrue(editor.isContentEmpty());

            editor.setValue("<p>   </p>");
            assertTrue(editor.isContentEmpty());

            editor.setValue("<p>Content</p>");
            assertFalse(editor.isContentEmpty());
        }
    }

    // ==================== Handler 集成测试 ====================

    @Nested
    @DisplayName("Handler 集成测试")
    class HandlerIntegrationTests {

        @Test
        @DisplayName("ErrorHandler 应被正确设置和获取")
        void testErrorHandler() {
            assertNull(editor.getErrorHandler());

            AtomicBoolean handled = new AtomicBoolean(false);
            ErrorHandler handler = error -> {
                handled.set(true);
                return true;
            };

            editor.setErrorHandler(handler);
            assertSame(handler, editor.getErrorHandler());
        }

        @Test
        @DisplayName("HtmlSanitizer 应被正确设置和获取")
        void testHtmlSanitizer() {
            assertNull(editor.getHtmlSanitizer());

            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.STRICT);
            editor.setHtmlSanitizer(sanitizer);
            assertSame(sanitizer, editor.getHtmlSanitizer());
        }

        @Test
        @DisplayName("getSanitizedValue 无 Sanitizer 应返回原值")
        void testGetSanitizedValueWithoutSanitizer() {
            String html = "<script>bad</script><p>good</p>";
            editor.setValue(html);
            assertEquals(html, editor.getSanitizedValue());
        }

        @Test
        @DisplayName("getSanitizedValue 有 Sanitizer 应清理内容")
        void testGetSanitizedValueWithSanitizer() {
            // Sanitizer 必须在 build 时设置，因为 ContentManager 在 initialize() 时创建
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.BASIC);
            VaadinCKEditor editorWithSanitizer = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withValue("<script>bad</script><p>good</p>")
                .withHtmlSanitizer(sanitizer)
                .build();

            String sanitized = editorWithSanitizer.getSanitizedValue();

            assertFalse(sanitized.contains("<script>"));
            assertTrue(sanitized.contains("good"));
        }
    }

    // ==================== 属性设置测试 ====================

    @Nested
    @DisplayName("属性设置测试")
    class PropertyTests {

        @Test
        @DisplayName("setReadOnly 应更新只读状态")
        void testSetReadOnly() {
            assertFalse(editor.isReadOnly());
            editor.setReadOnly(true);
            assertTrue(editor.isReadOnly());
            editor.setReadOnly(false);
            assertFalse(editor.isReadOnly());
        }

        @Test
        @DisplayName("setFallbackMode 应更新降级模式")
        void testSetFallbackMode() {
            assertEquals(FallbackMode.TEXTAREA, editor.getFallbackMode());

            editor.setFallbackMode(FallbackMode.READ_ONLY);
            assertEquals(FallbackMode.READ_ONLY, editor.getFallbackMode());

            editor.setFallbackMode(FallbackMode.ERROR_MESSAGE);
            assertEquals(FallbackMode.ERROR_MESSAGE, editor.getFallbackMode());

            editor.setFallbackMode(FallbackMode.HIDDEN);
            assertEquals(FallbackMode.HIDDEN, editor.getFallbackMode());
        }
    }

    // ==================== Builder 链式调用测试 ====================

    @Nested
    @DisplayName("Builder 链式调用测试")
    class BuilderChainTests {

        @Test
        @DisplayName("Builder 应支持完整链式调用")
        void testFullBuilderChain() {
            AtomicBoolean errorHandlerCalled = new AtomicBoolean(false);
            ErrorHandler errorHandler = error -> {
                errorHandlerCalled.set(true);
                return false;
            };
            HtmlSanitizer sanitizer = HtmlSanitizer.withPolicy(SanitizationPolicy.RELAXED);

            VaadinCKEditor fullEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.STANDARD)
                .withValue("<p>Initial</p>")
                .withLanguage("zh-cn")
                .withType(CKEditorType.CLASSIC)
                .withTheme(CKEditorTheme.DARK)
                .withFallbackMode(FallbackMode.READ_ONLY)
                .withErrorHandler(errorHandler)
                .withHtmlSanitizer(sanitizer)
                .build();

            // 设置只读模式（通过 setter）
            fullEditor.setReadOnly(true);

            assertEquals("<p>Initial</p>", fullEditor.getValue());
            assertTrue(fullEditor.isReadOnly());
            assertEquals(FallbackMode.READ_ONLY, fullEditor.getFallbackMode());
            assertSame(errorHandler, fullEditor.getErrorHandler());
            assertSame(sanitizer, fullEditor.getHtmlSanitizer());
        }

        @Test
        @DisplayName("withPreset 应快速创建编辑器")
        void testWithPresetShortcut() {
            VaadinCKEditor basicEditor = VaadinCKEditor.withPreset(CKEditorPreset.BASIC);
            assertNotNull(basicEditor);

            VaadinCKEditor standardEditor = VaadinCKEditor.withPreset(CKEditorPreset.STANDARD);
            assertNotNull(standardEditor);

            VaadinCKEditor fullEditor = VaadinCKEditor.withPreset(CKEditorPreset.FULL);
            assertNotNull(fullEditor);
        }
    }

    // ==================== 插件配置测试 ====================

    @Nested
    @DisplayName("插件配置测试")
    class PluginConfigTests {

        @Test
        @DisplayName("addPlugin 应添加插件")
        void testAddPlugin() {
            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .addPlugin(CKEditorPlugin.TABLE)
                .addPlugin(CKEditorPlugin.CODE_BLOCK)
                .build();

            assertNotNull(customEditor);
        }

        @Test
        @DisplayName("withPlugins 应设置插件集合")
        void testWithPlugins() {
            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPlugins(
                    CKEditorPlugin.ESSENTIALS,
                    CKEditorPlugin.PARAGRAPH,
                    CKEditorPlugin.BOLD,
                    CKEditorPlugin.ITALIC
                )
                .build();

            assertNotNull(customEditor);
        }

        @Test
        @DisplayName("依赖模式应正确设置")
        void testDependencyMode() {
            VaadinCKEditor autoEditor = VaadinCKEditor.create()
                .withPlugins(CKEditorPlugin.IMAGE_CAPTION)
                .withDependencyMode(VaadinCKEditorBuilder.DependencyMode.AUTO_RESOLVE)
                .build();

            assertNotNull(autoEditor);
        }

        @Test
        @DisplayName("STRICT 模式缺少依赖应抛出异常")
        void testStrictModeThrowsOnMissingDependency() {
            assertThrows(IllegalStateException.class, () -> {
                VaadinCKEditor.create()
                    .withPlugins(CKEditorPlugin.IMAGE_CAPTION) // 需要 IMAGE 依赖
                    .withDependencyMode(VaadinCKEditorBuilder.DependencyMode.STRICT)
                    .build();
            });
        }
    }

    // ==================== 工具栏配置测试 ====================

    @Nested
    @DisplayName("工具栏配置测试")
    class ToolbarConfigTests {

        @Test
        @DisplayName("withToolbar 应设置自定义工具栏")
        void testCustomToolbar() {
            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withToolbar("bold", "italic", "|", "undo", "redo")
                .build();

            assertNotNull(customEditor);
        }

        @Test
        @DisplayName("withToolbar 支持分隔符")
        void testToolbarWithSeparator() {
            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withToolbar("bold", "italic", "|", "bulletedList", "numberedList")
                .build();

            assertNotNull(customEditor);
        }
    }

    // ==================== 配置对象测试 ====================

    @Nested
    @DisplayName("配置对象测试")
    class ConfigTests {

        @Test
        @DisplayName("withConfig 应应用配置")
        void testWithConfig() {
            CKEditorConfig config = new CKEditorConfig();
            config.setPlaceholder("请输入内容...");

            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withConfig(config)
                .build();

            assertNotNull(customEditor);
        }

        @Test
        @DisplayName("withConfig 应允许详细配置")
        void testWithConfigDetailed() {
            CKEditorConfig config = new CKEditorConfig();
            config.setPlaceholder("Type here...");

            VaadinCKEditor customEditor = VaadinCKEditor.create()
                .withPreset(CKEditorPreset.BASIC)
                .withConfig(config)
                .build();

            assertNotNull(customEditor);
        }
    }

    // ==================== 版本信息测试 ====================

    @Nested
    @DisplayName("版本信息测试")
    class VersionTests {

        @Test
        @DisplayName("getVersion 应返回版本号")
        void testGetVersion() {
            String version = VaadinCKEditor.getVersion();
            assertNotNull(version);
            assertFalse(version.isEmpty());
            assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"));
        }
    }

    // ==================== 并发安全测试 ====================

    @Nested
    @DisplayName("并发安全测试")
    class ConcurrencyTests {

        @Test
        @DisplayName("并发注册监听器应线程安全")
        void testConcurrentListenerRegistration() throws InterruptedException {
            int threadCount = 10;
            int listenersPerThread = 100;
            AtomicInteger registrationCount = new AtomicInteger(0);

            Thread[] threads = new Thread[threadCount];
            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < listenersPerThread; j++) {
                        editor.addEditorReadyListener(event -> {});
                        registrationCount.incrementAndGet();
                    }
                });
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            assertEquals(threadCount * listenersPerThread, registrationCount.get());
            assertEquals(threadCount * listenersPerThread, editor.getListenerStats().ready);
        }

        @Test
        @DisplayName("并发清理监听器应线程安全")
        void testConcurrentCleanup() throws InterruptedException {
            // 先注册一些监听器
            for (int i = 0; i < 100; i++) {
                editor.addEditorReadyListener(event -> {});
            }

            // 并发清理
            Thread[] threads = new Thread[5];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread(() -> editor.cleanupListeners());
            }

            for (Thread thread : threads) {
                thread.start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            // 清理后应该为零
            assertEquals(0, editor.getListenerStats().total());
        }
    }
}
