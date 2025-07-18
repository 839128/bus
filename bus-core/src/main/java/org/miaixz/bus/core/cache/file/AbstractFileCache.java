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
package org.miaixz.bus.core.cache.file;

import java.io.File;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.cache.Cache;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * 文件缓存，以解决频繁读取文件引起的性能问题
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFileCache implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852230175903L;

    /**
     * 容量
     */
    protected final int capacity;
    /**
     * 缓存的最大文件大小，文件大于此大小时将不被缓存
     */
    protected final int maxFileSize;
    /**
     * 默认超时时间，0表示无默认超时
     */
    protected final long timeout;
    /**
     * 缓存实现
     */
    protected final Cache<File, byte[]> cache;

    /**
     * 已使用缓存空间
     */
    protected int usedSize;

    /**
     * 构造
     *
     * @param capacity    缓存容量
     * @param maxFileSize 文件最大大小
     * @param timeout     默认超时时间，0表示无默认超时
     */
    public AbstractFileCache(final int capacity, final int maxFileSize, final long timeout) {
        this.capacity = capacity;
        this.maxFileSize = maxFileSize;
        this.timeout = timeout;
        this.cache = initCache();
    }

    /**
     * 缓存容量
     *
     * @return 缓存容量（byte数）
     */
    public int capacity() {
        return capacity;
    }

    /**
     * 已使用空间大小
     *
     * @return 已使用空间大小（byte数）
     */
    public int getUsedSize() {
        return usedSize;
    }

    /**
     * 允许被缓存文件的最大byte数
     *
     * @return 允许被缓存文件的最大byte数
     */
    public int maxFileSize() {
        return maxFileSize;
    }

    /**
     * @return 缓存的文件数
     */
    public int getCachedFilesCount() {
        return cache.size();
    }

    /**
     * 超时时间
     *
     * @return 超时时间
     */
    public long timeout() {
        return this.timeout;
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        usedSize = 0;
    }

    /**
     * 获得缓存过的文件bytes
     *
     * @param path 文件路径
     * @return 缓存过的文件bytes
     * @throws InternalException IO异常
     */
    public byte[] getFileBytes(final String path) throws InternalException {
        return getFileBytes(new File(path));
    }

    /**
     * 获得缓存过的文件bytes
     *
     * @param file 文件
     * @return 缓存过的文件bytes
     * @throws InternalException IO异常
     */
    public byte[] getFileBytes(final File file) throws InternalException {
        byte[] bytes = cache.get(file);
        if (bytes != null) {
            return bytes;
        }

        // 读取文件的所有内容
        bytes = FileKit.readBytes(file);

        if ((maxFileSize != 0) && (file.length() > maxFileSize)) {
            // 大于缓存空间，不缓存，直接返回
            return bytes;
        }

        usedSize += bytes.length;

        // 文件放入缓存，如果usedSize > capacity，purge()方法将被调用
        cache.put(file, bytes);

        return bytes;
    }

    /**
     * 初始化实现文件缓存的缓存对象
     *
     * @return {@link Cache}
     */
    protected abstract Cache<File, byte[]> initCache();

}
