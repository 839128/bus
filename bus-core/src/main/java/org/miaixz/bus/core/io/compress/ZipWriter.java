/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.compress;

import java.io.*;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * Zip生成封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipWriter implements Closeable {

    /**
     * 原始输出流
     */
    private final ZipOutputStream out;
    /**
     * Zip文件
     */
    private File zipFile;
    /**
     * 自定义缓存大小
     */
    private int bufferSize = Normal._8192;

    /**
     * 构造
     *
     * @param zipFile 生成的Zip文件
     * @param charset 编码
     */
    public ZipWriter(final File zipFile, final Charset charset) {
        this(getZipOutputStream(zipFile, charset));
        this.zipFile = zipFile;
    }

    /**
     * 构造
     *
     * @param out     {@link ZipOutputStream}
     * @param charset 编码
     */
    public ZipWriter(final OutputStream out, final Charset charset) {
        this(ZipKit.getZipOutputStream(out, charset));
    }

    /**
     * 构造
     *
     * @param out {@link ZipOutputStream}
     */
    public ZipWriter(final ZipOutputStream out) {
        this.out = out;
    }

    /**
     * 创建ZipWriter
     *
     * @param zipFile 生成的Zip文件
     * @param charset 编码
     * @return ZipWriter
     */
    public static ZipWriter of(final File zipFile, final Charset charset) {
        return new ZipWriter(zipFile, charset);
    }

    /**
     * 创建ZipWriter
     *
     * @param out     Zip输出的流，一般为输出文件流
     * @param charset 编码
     * @return ZipWriter
     */
    public static ZipWriter of(final OutputStream out, final Charset charset) {
        return new ZipWriter(out, charset);
    }

    /**
     * 获得 {@link ZipOutputStream}
     *
     * @param zipFile 压缩文件
     * @param charset 编码
     * @return {@link ZipOutputStream}
     */
    private static ZipOutputStream getZipOutputStream(final File zipFile, final Charset charset) {
        return ZipKit.getZipOutputStream(FileKit.getOutputStream(zipFile), charset);
    }

    /**
     * 自定义压缩缓存大小，特定条件下调节性能
     *
     * @param bufferSize 缓存大小
     * @return this
     */
    public ZipWriter setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    /**
     * 设置压缩级别，可选1~9，-1表示默认
     *
     * @param level 压缩级别
     * @return this
     */
    public ZipWriter setLevel(final int level) {
        this.out.setLevel(level);
        return this;
    }

    /**
     * 设置注释
     *
     * @param comment 注释
     * @return this
     */
    public ZipWriter setComment(final String comment) {
        this.out.setComment(comment);
        return this;
    }

    /**
     * 设置压缩方式
     *
     * @param method 压缩方式，支持{@link ZipOutputStream#DEFLATED}和{@link ZipOutputStream#STORED}
     * @return this
     */
    public ZipWriter setMethod(final int method) {
        this.out.setMethod(method);
        return this;
    }

    /**
     * 获取原始的{@link ZipOutputStream}
     *
     * @return {@link ZipOutputStream}
     */
    public ZipOutputStream getOut() {
        return this.out;
    }

    /**
     * 对文件或文件目录进行压缩
     *
     * @param withSrcDir 是否包含被打包目录，只针对压缩目录有效。若为false，则只压缩目录下的文件或目录，为true则将本目录也压缩
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩），{@code null}表示不过滤
     * @param files      要压缩的源文件或目录。如果压缩一个文件，则为该文件的全路径；如果压缩一个目录，则为该目录的顶层目录路径
     * @return this
     * @throws InternalException IO异常
     */
    public ZipWriter add(final boolean withSrcDir, final FileFilter filter, final File... files)
            throws InternalException {
        for (final File file : files) {
            // 如果只是压缩一个文件，则需要截取该文件的父目录
            String srcRootDir;
            try {
                srcRootDir = file.getCanonicalPath();
                if ((!file.isDirectory()) || withSrcDir) {
                    // 若是文件，则将父目录完整路径都截取掉；若设置包含目录，则将上级目录全部截取掉，保留本目录名
                    srcRootDir = file.getCanonicalFile().getParentFile().getCanonicalPath();
                }
            } catch (final IOException e) {
                throw new InternalException(e);
            }

            _add(file, srcRootDir, filter);
        }
        return this;
    }

    /**
     * 添加资源到压缩包，添加后关闭资源流
     *
     * @param resources 需要压缩的资源，资源的路径为{@link Resource#getName()}
     * @return this
     * @throws InternalException IO异常
     */
    public ZipWriter add(final Resource... resources) throws InternalException {
        for (final Resource resource : resources) {
            if (null != resource) {
                add(resource.getName(), resource.getStream());
            }
        }
        return this;
    }

    /**
     * 添加文件流到压缩包，添加后关闭输入文件流 如果输入流为{@code null}，则只创建空目录
     *
     * @param path 压缩的路径, {@code null}和""表示根目录下
     * @param in   需要压缩的输入流，使用完后自动关闭，{@code null}表示加入空目录
     * @return this
     * @throws InternalException IO异常
     */
    public ZipWriter add(String path, final InputStream in) throws InternalException {
        path = StringKit.toStringOrEmpty(path);
        if (null == in) {
            // 空目录需要检查路径规范性，目录以"/"结尾
            path = StringKit.addSuffixIfNot(path, Symbol.SLASH);
            if (StringKit.isBlank(path)) {
                return this;
            }
        }

        return putEntry(path, in);
    }

    /**
     * 对流中的数据加入到压缩文件 路径列表和流列表长度必须一致
     *
     * @param paths 流数据在压缩文件中的路径或文件名
     * @param ins   要压缩的源，添加完成后自动关闭流
     * @return 压缩文件
     * @throws InternalException IO异常
     */
    public ZipWriter add(final String[] paths, final InputStream[] ins) throws InternalException {
        if (ArrayKit.isEmpty(paths) || ArrayKit.isEmpty(ins)) {
            throw new IllegalArgumentException("Paths or ins is empty !");
        }
        if (paths.length != ins.length) {
            throw new IllegalArgumentException("Paths length is not equals to ins length !");
        }

        for (int i = 0; i < paths.length; i++) {
            add(paths[i], ins[i]);
        }

        return this;
    }

    @Override
    public void close() throws InternalException {
        try {
            out.finish();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(this.out);
        }
    }

    /**
     * 递归压缩文件夹或压缩文件 srcRootDir决定了路径截取的位置，例如： file的路径为d:/a/b/c/d.txt，srcRootDir为d:/a/b，则压缩后的文件与目录为结构为c/d.txt
     *
     * @param srcRootDir 被压缩的文件夹根目录
     * @param file       当前递归压缩的文件或目录对象
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩），{@code null}表示不过滤
     * @throws InternalException IO异常
     */
    private void _add(final File file, final String srcRootDir, final FileFilter filter) throws InternalException {
        if (null == file || (null != filter && !filter.accept(file))) {
            return;
        }

        // 获取文件相对于压缩文件夹根目录的子路径
        final String subPath = FileKit.subPath(srcRootDir, file);
        if (file.isDirectory()) {
            // 如果是目录，则压缩压缩目录中的文件或子目录
            final File[] files = file.listFiles();
            if (ArrayKit.isEmpty(files)) {
                // 加入目录，只有空目录时才加入目录，非空时会在创建文件时自动添加父级目录
                add(subPath, null);
            } else {
                // 压缩目录下的子文件或目录
                for (final File childFile : files) {
                    _add(childFile, srcRootDir, filter);
                }
            }
        } else {
            // 检查加入的文件是否为压缩结果文件本身，避免死循环
            if (FileKit.equals(file, zipFile)) {
                return;
            }
            // 如果是文件或其它符号，则直接压缩该文件
            putEntry(subPath, FileKit.getInputStream(file));
        }
    }

    /**
     * 添加文件流到压缩包，添加后关闭输入文件流 如果输入流为{@code null}，则只创建空目录
     *
     * @param path 压缩的路径, {@code null}和""表示根目录下
     * @param in   需要压缩的输入流，使用完后自动关闭，{@code null}表示加入空目录
     * @throws InternalException IO异常
     */
    private ZipWriter putEntry(final String path, final InputStream in) throws InternalException {
        final ZipEntry entry = new ZipEntry(path);
        final ZipOutputStream out = this.out;
        try {
            out.putNextEntry(entry);
            if (null != in) {
                IoKit.copy(in, out, bufferSize);
            }
            out.closeEntry();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }

        IoKit.flush(out);
        return this;
    }

}
