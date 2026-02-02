package com.wontlost.ckeditor.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

/**
 * 文件上传处理器。
 * 用于处理编辑器中的图片和文件上传。
 *
 * <p>使用示例：</p>
 * <pre>
 * // 上传到本地文件系统
 * editor.setUploadHandler((context, stream) -&gt; {
 *     String filename = context.getFileName();
 *     Path targetPath = uploadDir.resolve(filename);
 *     Files.copy(stream, targetPath);
 *     return CompletableFuture.completedFuture(
 *         new UploadResult("/uploads/" + filename)
 *     );
 * });
 *
 * // 上传到云存储
 * editor.setUploadHandler((context, stream) -&gt; {
 *     return cloudStorage.uploadAsync(stream, context.getFileName())
 *         .thenApply(url -&gt; new UploadResult(url));
 * });
 * </pre>
 */
@FunctionalInterface
public interface UploadHandler {

    /**
     * 处理文件上传
     *
     * @param context 上传上下文信息
     * @param inputStream 文件输入流
     * @return 异步上传结果
     */
    CompletableFuture<UploadResult> handleUpload(UploadContext context, InputStream inputStream);

    /**
     * 上传上下文
     */
    class UploadContext {
        private final String fileName;
        private final String mimeType;
        private final long fileSize;

        /**
         * 创建上传上下文
         *
         * @param fileName 文件名
         * @param mimeType MIME 类型
         * @param fileSize 文件大小（字节）
         */
        public UploadContext(String fileName, String mimeType, long fileSize) {
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMimeType() {
            return mimeType;
        }

        public long getFileSize() {
            return fileSize;
        }

        /**
         * 检查是否为图片
         *
         * @return 如果是图片返回 true
         */
        public boolean isImage() {
            return mimeType != null && mimeType.startsWith("image/");
        }
    }

    /**
     * 上传结果
     */
    class UploadResult {
        private final String url;
        private final boolean success;
        private final String errorMessage;

        /**
         * 创建成功的上传结果
         *
         * @param url 上传后的访问 URL
         */
        public UploadResult(String url) {
            this.url = url;
            this.success = true;
            this.errorMessage = null;
        }

        /**
         * 创建失败的上传结果
         *
         * @param errorMessage 错误消息
         * @return 失败结果
         */
        public static UploadResult failure(String errorMessage) {
            return new UploadResult(null, false, errorMessage);
        }

        private UploadResult(String url, boolean success, String errorMessage) {
            this.url = url;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public String getUrl() {
            return url;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    /**
     * 上传配置
     */
    class UploadConfig {
        /** 最小允许的文件大小：1 字节 */
        public static final long MIN_FILE_SIZE = 1;
        /** 最大允许的文件大小：1GB */
        public static final long MAX_FILE_SIZE_LIMIT = 1024L * 1024 * 1024;
        /** 默认最大文件大小：10MB */
        public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024;

        private long maxFileSize = DEFAULT_MAX_FILE_SIZE;
        private java.util.Set<String> allowedMimeTypes = new java.util.LinkedHashSet<>(
            java.util.Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
        );

        /**
         * 获取最大文件大小
         *
         * @return 最大文件大小（字节）
         */
        public long getMaxFileSize() {
            return maxFileSize;
        }

        /**
         * 设置最大文件大小
         *
         * @param maxFileSize 最大文件大小（字节），必须在 1 字节到 1GB 之间
         * @return this
         * @throws IllegalArgumentException 如果 maxFileSize 超出有效范围
         */
        public UploadConfig setMaxFileSize(long maxFileSize) {
            if (maxFileSize < MIN_FILE_SIZE || maxFileSize > MAX_FILE_SIZE_LIMIT) {
                throw new IllegalArgumentException(
                    String.format("maxFileSize must be between %d and %d bytes, got %d",
                        MIN_FILE_SIZE, MAX_FILE_SIZE_LIMIT, maxFileSize));
            }
            this.maxFileSize = maxFileSize;
            return this;
        }

        /**
         * 获取允许的 MIME 类型
         *
         * @return MIME 类型数组的副本
         */
        public String[] getAllowedMimeTypes() {
            return allowedMimeTypes.toArray(new String[0]);
        }

        /**
         * 设置允许的 MIME 类型。
         * 设置为空数组将允许所有 MIME 类型。
         *
         * @param allowedMimeTypes MIME 类型数组
         * @return this
         * @throws IllegalArgumentException 如果数组为 null 或包含 null/空字符串
         */
        public UploadConfig setAllowedMimeTypes(String... allowedMimeTypes) {
            if (allowedMimeTypes == null) {
                throw new IllegalArgumentException("allowedMimeTypes cannot be null");
            }
            this.allowedMimeTypes = new java.util.LinkedHashSet<>();
            for (String mimeType : allowedMimeTypes) {
                if (mimeType == null || mimeType.trim().isEmpty()) {
                    throw new IllegalArgumentException("MIME type cannot be null or empty");
                }
                this.allowedMimeTypes.add(mimeType.trim());
            }
            return this;
        }

        /**
         * 添加额外的 MIME 类型到允许列表
         *
         * @param mimeTypes 要添加的 MIME 类型
         * @return this
         */
        public UploadConfig addAllowedMimeTypes(String... mimeTypes) {
            if (mimeTypes != null) {
                for (String mimeType : mimeTypes) {
                    if (mimeType != null && !mimeType.trim().isEmpty()) {
                        this.allowedMimeTypes.add(mimeType.trim());
                    }
                }
            }
            return this;
        }

        /**
         * 重置为默认的 MIME 类型列表
         *
         * @return this
         */
        public UploadConfig resetAllowedMimeTypes() {
            this.allowedMimeTypes = new java.util.LinkedHashSet<>(
                java.util.Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp")
            );
            return this;
        }

        /**
         * 验证上传是否符合配置
         *
         * @param context 上传上下文
         * @return 验证失败的错误消息，验证成功返回 null
         */
        public String validate(UploadContext context) {
            if (context == null) {
                return "Upload context cannot be null";
            }

            if (context.getFileSize() > maxFileSize) {
                return String.format("File size %d exceeds maximum allowed %d bytes",
                    context.getFileSize(), maxFileSize);
            }

            // 空的允许列表表示允许所有类型
            if (!allowedMimeTypes.isEmpty() && !allowedMimeTypes.contains(context.getMimeType())) {
                return String.format("MIME type '%s' is not allowed. Allowed types: %s",
                    context.getMimeType(), String.join(", ", allowedMimeTypes));
            }

            return null;
        }
    }
}
