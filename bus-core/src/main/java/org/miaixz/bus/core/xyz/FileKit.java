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
package org.miaixz.bus.core.xyz;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.io.BomReader;
import org.miaixz.bus.core.io.file.*;
import org.miaixz.bus.core.io.file.FileReader;
import org.miaixz.bus.core.io.file.FileWriter;
import org.miaixz.bus.core.io.resource.FileResource;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.io.stream.BOMInputStream;
import org.miaixz.bus.core.io.stream.LineCounter;
import org.miaixz.bus.core.io.unit.DataSize;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * 文件工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileKit extends PathResolve {

    /**
     * 绝对路径判断正则
     */
    private static final Pattern PATTERN_PATH_ABSOLUTE = Pattern.compile("^[a-zA-Z]:([/\\\\].*)?", Pattern.DOTALL);

    /**
     * 是否为Windows环境
     *
     * @return 是否为Windows环境
     */
    public static boolean isWindows() {
        return Symbol.C_BACKSLASH == File.separatorChar;
    }

    /**
     * 列出指定路径下的目录和文件 给定的绝对路径不能是压缩包中的路径
     *
     * @param path 目录绝对路径或者相对路径
     * @return 文件列表（包含目录）
     */
    public static File[] ls(final String path) {
        if (path == null) {
            return null;
        }

        final File file = file(path);
        if (file.isDirectory()) {
            return file.listFiles();
        }
        throw new InternalException(StringKit.format("Path [{}] is not directory!", path));
    }

    /**
     * 文件是否为空 目录：里面没有文件时为空 文件：文件大小为0时为空
     *
     * @param file 文件
     * @return 是否为空，当提供非目录时，返回false
     */
    public static boolean isEmpty(final File file) {
        if (null == file || !file.exists()) {
            return true;
        }

        if (file.isDirectory()) {
            final String[] subFiles = file.list();
            return ArrayKit.isEmpty(subFiles);
        } else if (file.isFile()) {
            return file.length() <= 0;
        }

        return false;
    }

    /**
     * 文件是不为空 目录：里面有文件或目录 文件：文件大小大于0时
     *
     * @param file 目录
     * @return 是否为空，当提供非目录时，返回false
     */
    public static boolean isNotEmpty(final File file) {
        return !isEmpty(file);
    }

    /**
     * 目录是否为空
     *
     * @param dir 目录
     * @return 是否为空
     */
    public static boolean isDirEmpty(final File dir) {
        return isDirEmpty(dir.toPath());
    }

    /**
     * 递归遍历目录以及子目录中的所有文件 如果提供file为文件，直接返回过滤结果
     *
     * @param path       当前遍历文件或目录的路径
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录
     * @return 文件列表
     */
    public static List<File> loopFiles(final String path, final FileFilter fileFilter) {
        return loopFiles(file(path), fileFilter);
    }

    /**
     * 递归遍历目录以及子目录中的所有文件 如果提供file为文件，直接返回过滤结果
     *
     * @param file       当前遍历文件或目录
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录
     * @return 文件列表
     */
    public static List<File> loopFiles(final File file, final FileFilter fileFilter) {
        return loopFiles(file, -1, fileFilter);
    }

    /**
     * 递归遍历目录以及子目录中的所有文件 如果提供file为文件，直接返回过滤结果
     *
     * @param file       当前遍历文件或目录
     * @param maxDepth   遍历最大深度，-1表示遍历到没有目录为止
     * @param fileFilter 文件过滤规则对象，选择要保留的文件，只对文件有效，不过滤目录，null表示接收全部文件
     * @return 文件列表
     */
    public static List<File> loopFiles(final File file, final int maxDepth, final FileFilter fileFilter) {
        return loopFiles(file.toPath(), maxDepth, fileFilter);
    }

    /**
     * 递归遍历目录以及子目录中的所有文件 如果用户传入相对路径，则是相对classpath的路径 如："test/aaa"表示"${classpath}/test/aaa"
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 文件列表
     */
    public static List<File> loopFiles(final String path) {
        return loopFiles(file(path));
    }

    /**
     * 递归遍历目录以及子目录中的所有文件
     *
     * @param file 当前遍历文件
     * @return 文件列表
     */
    public static List<File> loopFiles(final File file) {
        return loopFiles(file, null);
    }

    /**
     * 递归遍历目录并处理目录下的文件，可以处理目录或文件：
     * <ul>
     * <li>目录和非目录均调用{@link Predicate}处理</li>
     * <li>目录如果{@link Predicate#test(Object)}为{@code true}则递归调用此方法处理。</li>
     * </ul>
     * 此方法与{@link #loopFiles(File, FileFilter)}不同的是，处理目录判断，可减少无效目录的遍历。
     *
     * @param file      文件或目录，文件直接处理
     * @param predicate 文件处理器，只会处理文件
     */
    public static void walkFiles(final File file, final Predicate<File> predicate) {
        if (predicate.test(file) && file.isDirectory()) {
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isNotEmpty(subFiles)) {
                for (final File tmp : subFiles) {
                    walkFiles(tmp, predicate);
                }
            }
        }
    }

    /**
     * 获得指定目录下所有文件 不会扫描子目录 如果用户传入相对路径，则是相对classpath的路径 如："test/aaa"表示"${classpath}/test/aaa"
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 文件路径列表（如果是jar中的文件，则给定类似.jar!/xxx/xxx的路径）
     * @throws InternalException IO异常
     */
    public static List<String> listFileNames(String path) throws InternalException {
        if (path == null) {
            return new ArrayList<>(0);
        }
        int index = path.lastIndexOf(FileType.JAR_PATH_EXT);
        if (index < 0) {
            // 普通目录
            final List<String> paths = new ArrayList<>();
            final File[] files = ls(path);
            for (final File file : files) {
                if (file.isFile()) {
                    paths.add(file.getName());
                }
            }
            return paths;
        }

        // jar文件
        path = getAbsolutePath(path);
        // jar文件中的路径
        index = index + FileType.JAR.length();
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(path.substring(0, index));
            // 防止出现jar!/cn/miaixz/这类路径导致文件找不到
            return ZipKit.listFileNames(jarFile, StringKit.removePrefix(path.substring(index + 1), "/"));
        } catch (final IOException e) {
            throw new InternalException(StringKit.format("Can not read file path of [{}]", path), e);
        } finally {
            IoKit.closeQuietly(jarFile);
        }
    }

    /**
     * 创建File对象，相当于调用new File()，不做任何处理 相对于项目路径，如`project:./test`且项目路径为`/workspace/bus/`，则读取`/workspace/bus/test`
     *
     * @param path 文件路径，相对路径表示相对项目路径
     * @return File
     */
    public static File newFile(final String path) {
        return new File(path);
    }

    /**
     * 创建File对象，自动识别相对或绝对路径，规则如下：
     * <ul>
     * <li>如果为绝对路径，如Linux下以`/`开头，Win下以如`d:\`开头，则直接使用。</li>
     * <li>如果以`classpath:`开头或`file:`开头，直接去掉。</li>
     * <li>如果为相对路径，如`./xxx`或`xx/xx`则理解为相对路径，相对路径全部相对于classpath。</li>
     * <li>如果以`project:`开头，且为相对路径，则使用JDK默认规则，相对于项目路径，如`project:./test`且项目路径为`/workspace/bus/`，则读取`/workspace/bus/test`</li>
     * </ul>
     * <p>
     * ，相对路径将自动从ClassPath下寻找 如果用户需要相对项目路径，则使用project:前缀
     *
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return File
     */
    public static File file(final String path) {
        if (null == path) {
            return null;
        }

        // 如果用户需要相对项目路径，则使用project:前缀
        if (path.startsWith(Normal.PROJECT_URL_PREFIX)) {
            return new File(StringKit.subSuf(path, Normal.PROJECT_URL_PREFIX.length()));
        }

        return new File(getAbsolutePath(path));
    }

    /**
     * 创建File对象 此方法会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parent 父目录
     * @param path   文件路径
     * @return File
     */
    public static File file(final String parent, final String path) {
        return file(new File(parent), path);
    }

    /**
     * 创建File对象 根据的路径构建文件，在Win下直接构建，在Linux下拆分路径单独构建 此方法会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     */
    public static File file(final File parent, final String path) {
        if (StringKit.isBlank(path)) {
            throw new NullPointerException("File path is blank!");
        }
        return checkSlip(parent, buildFile(parent, path));
    }

    /**
     * 通过多层目录参数创建文件 此方法会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param directory 父目录
     * @param names     元素名（多层目录名），由外到内依次传入
     * @return the file 文件
     */
    public static File file(final File directory, final String... names) {
        Assert.notNull(directory, "directory must not be null");
        if (ArrayKit.isEmpty(names)) {
            return directory;
        }

        File file = directory;
        for (final String name : names) {
            if (null != name) {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * 通过多层目录创建文件 元素名（多层目录名）
     *
     * @param names 多层文件的文件名，由外到内依次传入
     * @return the file 文件
     */
    public static File file(final String... names) {
        if (ArrayKit.isEmpty(names)) {
            return null;
        }

        File file = null;
        for (final String name : names) {
            if (file == null) {
                file = file(name);
            } else {
                file = file(file, name);
            }
        }
        return file;
    }

    /**
     * 创建File对象
     *
     * @param uri 文件URI
     * @return File
     */
    public static File file(final URI uri) {
        if (uri == null) {
            throw new NullPointerException("File uri is null!");
        }
        return new File(uri);
    }

    /**
     * 创建File对象
     *
     * @param url 文件URL
     * @return File
     */
    public static File file(final URL url) {
        return new File(UrlKit.toURI(url));
    }

    /**
     * 获取临时文件目录
     *
     * @return 临时文件目录
     */
    public static File getTmpDir() {
        return file(Keys.getTmpDirPath());
    }

    /**
     * 获取用户目录
     *
     * @return 用户目录
     */
    public static File getUserHomeDir() {
        return file(Keys.getUserHomePath());
    }

    /**
     * 判断文件是否存在，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果存在返回true
     */
    public static boolean exists(final String path) {
        return (null != path) && file(path).exists();
    }

    /**
     * 判断文件是否存在，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果存在返回true
     */
    public static boolean exists(final File file) {
        return (null != file) && file.exists();
    }

    /**
     * 是否存在匹配文件
     *
     * @param directory 文件夹路径
     * @param regexp    文件夹中所包含文件名的正则表达式
     * @return 如果存在匹配文件返回true
     */
    public static boolean exists(final String directory, final String regexp) {
        final File file = new File(directory);
        if (!file.exists()) {
            return false;
        }

        final String[] fileList = file.list();
        if (fileList == null) {
            return false;
        }

        for (final String fileName : fileList) {
            if (fileName.matches(regexp)) {
                return true;
            }

        }
        return false;
    }

    /**
     * 指定文件最后修改时间
     *
     * @param file 文件
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(final File file) {
        if (!exists(file)) {
            return null;
        }

        return new Date(file.lastModified());
    }

    /**
     * 指定路径文件最后修改时间
     *
     * @param path 绝对路径
     * @return 最后修改时间
     */
    public static Date lastModifiedTime(final String path) {
        return lastModifiedTime(new File(path));
    }

    /**
     * 计算目录或文件的总大小 当给定对象为文件时，直接调用 {@link File#length()} 当给定对象为目录时，遍历目录下的所有文件和目录，递归计算其大小，求和返回 此方法不包括目录本身的占用空间大小。
     *
     * @param file 目录或文件,null或者文件不存在返回0
     * @return 总大小，bytes长度
     */
    public static long size(final File file) {
        return size(file, false);
    }

    /**
     * 计算目录或文件的总大小 当给定对象为文件时，直接调用 {@link File#length()} 当给定对象为目录时，遍历目录下的所有文件和目录，递归计算其大小，求和返回
     *
     * @param file           目录或文件,null或者文件不存在返回0
     * @param includeDirSize 是否包括每层目录本身的大小
     * @return 总大小，bytes长度
     */
    public static long size(final File file, final boolean includeDirSize) {
        if (null == file || !file.exists() || isSymlink(file)) {
            return 0;
        }

        if (file.isDirectory()) {
            long size = includeDirSize ? file.length() : 0;
            final File[] subFiles = file.listFiles();
            if (ArrayKit.isEmpty(subFiles)) {
                return 0L;// empty directory
            }
            for (final File subFile : subFiles) {
                size += size(subFile, includeDirSize);
            }
            return size;
        } else {
            return file.length();
        }
    }

    /**
     * 计算文件的总行数 读取文件采用系统默认编码，一般乱码不会造成行数错误。
     *
     * @param file 文件
     * @return 该文件总行数
     */
    public static int getTotalLines(final File file) {
        return getTotalLines(file, -1);
    }

    /**
     * 计算文件的总行数 参考：https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java 最后一行如果末尾带有换行符，则被当作为新行
     *
     * @param file       文件
     * @param bufferSize 缓存大小，小于1则使用默认的1024
     * @return 该文件总行数
     */
    public static int getTotalLines(final File file, int bufferSize) {
        return getTotalLines(file, bufferSize, true);
    }

    /**
     * 计算文件的总行数 参考：https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java 最后一行如果末尾带有换行符，则被当作为新行
     *
     * @param file                       文件
     * @param bufferSize                 缓存大小，小于1则使用默认的1024
     * @param lastLineSeparatorAsNewLine 是否将最后一行分隔符作为新行，Linux下要求最后一行必须带有换行符，不算一行，此处用户选择
     * @return 该文件总行数
     */
    public static int getTotalLines(final File file, final int bufferSize, final boolean lastLineSeparatorAsNewLine) {
        Assert.isTrue(isFile(file), () -> new InternalException("Input must be a File"));
        try (final LineCounter lineCounter = new LineCounter(getInputStream(file), bufferSize)) {
            lineCounter.setLastLineSeparatorAsNewLine(lastLineSeparatorAsNewLine);
            return lineCounter.getCount();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file      文件或目录
     * @param reference 参照文件
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(final File file, final File reference) {
        if (null == reference || !reference.exists()) {
            return true;// 文件一定比一个不存在的文件新
        }
        return newerThan(file, reference.lastModified());
    }

    /**
     * 给定文件或目录的最后修改时间是否晚于给定时间
     *
     * @param file       文件或目录
     * @param timeMillis 做为对比的时间
     * @return 是否晚于给定时间
     */
    public static boolean newerThan(final File file, final long timeMillis) {
        if (null == file || !file.exists()) {
            return false;// 不存在的文件一定比任何时间旧
        }
        return file.lastModified() > timeMillis;
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param path 相对ClassPath的目录或者绝对路径目录，使用POSIX风格
     * @return 文件，若路径为null，返回null
     * @throws InternalException IO异常
     */
    public static File touch(final String path) throws InternalException {
        if (path == null) {
            return null;
        }
        return touch(file(path));
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param file 文件对象
     * @return 文件，若路径为null，返回null
     * @throws InternalException IO异常
     */
    public static File touch(final File file) throws InternalException {
        if (null == file) {
            return null;
        }
        if (!file.exists()) {
            mkParentDirs(file);
            try {
                // noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (final Exception e) {
                throw new InternalException(e);
            }
        }
        return file;
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws InternalException IO异常
     */
    public static File touch(final File parent, final String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     * @throws InternalException IO异常
     */
    public static File touch(final String parent, final String path) throws InternalException {
        return touch(file(parent, path));
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(final File file) {
        if (null == file) {
            return null;
        }
        return mkdir(getParent(file, 1));
    }

    /**
     * 创建父文件夹，如果存在直接返回此文件夹
     *
     * @param path 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkParentDirs(final String path) {
        if (path == null) {
            return null;
        }
        return mkParentDirs(file(path));
    }

    /**
     * 删除文件或者文件夹 路径如果为相对路径，会转换为ClassPath路径！ 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹 某个文件删除失败会终止删除操作
     *
     * @param fullFileOrDirPath 文件或者目录的路径
     * @throws InternalException IO异常
     */
    public static void remove(final String fullFileOrDirPath) throws InternalException {
        remove(file(fullFileOrDirPath));
    }

    /**
     * 删除文件或者文件夹 注意：删除文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹 某个文件删除失败会终止删除操作
     *
     * @param file 文件对象
     * @throws InternalException IO异常
     * @see Files#delete(Path)
     */
    public static void remove(final File file) throws InternalException {
        Assert.notNull(file, "File must be not null!");
        remove(file.toPath());
    }

    /**
     * 清空文件夹 注意：清空文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹 某个文件删除失败会终止删除操作
     *
     * @param dirPath 文件夹路径
     * @throws InternalException IO异常
     */
    public static void clean(final String dirPath) throws InternalException {
        clean(file(dirPath));
    }

    /**
     * 清空文件夹 注意：清空文件夹时不会判断文件夹是否为空，如果不空则递归删除子文件或文件夹 某个文件删除失败会终止删除操作
     *
     * @param directory 文件夹
     * @throws InternalException IO异常
     */
    public static void clean(final File directory) throws InternalException {
        Assert.notNull(directory, "File must be not null!");
        clean(directory.toPath());
    }

    /**
     * 清理空文件夹 此方法用于递归删除空的文件夹，不删除文件 如果传入的文件夹本身就是空的，删除这个文件夹
     *
     * @param directory 文件夹
     * @return the true/false
     */
    public static boolean cleanEmpty(final File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return true;
        }

        final File[] files = directory.listFiles();
        if (ArrayKit.isEmpty(files)) {
            // 空文件夹则删除之
            return directory.delete();
        }

        for (final File childFile : files) {
            cleanEmpty(childFile);
        }

        // 当前目录清除完毕，需要再次判断当前文件夹，空文件夹则删除之
        final String[] fileNames = directory.list();
        if (ArrayKit.isEmpty(fileNames)) {
            return directory.delete();
        }
        return true;
    }

    /**
     * 创建文件夹，如果存在直接返回此文件夹 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dirPath 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkdir(final String dirPath) {
        if (dirPath == null) {
            return null;
        }
        final File dir = file(dirPath);
        return mkdir(dir);
    }

    /**
     * 创建文件夹，会递归自动创建其不存在的父文件夹，如果存在直接返回此文件夹 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dir 目录
     * @return 创建的目录
     */
    public static File mkdir(final File dir) {
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            mkdirsSafely(dir, 5, 1);
        }
        return dir;
    }

    /**
     * 安全地级联创建目录 (确保并发环境下能创建成功)
     *
     * <pre>
     *     并发环境下，假设 test 目录不存在，如果线程A mkdirs "test/A" 目录，线程B mkdirs "test/B"目录，
     *     其中一个线程可能会失败，进而导致以下代码抛出 FileNotFoundException 异常
     *
     *     file.getParentFile().mkdirs(); // 父目录正在被另一个线程创建中，返回 false
     *     file.createNewFile(); // 抛出 IO 异常，因为该线程无法感知到父目录已被创建
     * </pre>
     *
     * @param dir         待创建的目录
     * @param tryCount    最大尝试次数
     * @param sleepMillis 线程等待的毫秒数
     * @return true表示创建成功，false表示创建失败
     */
    public static boolean mkdirsSafely(final File dir, final int tryCount, final long sleepMillis) {
        if (dir == null) {
            return false;
        }
        if (dir.isDirectory()) {
            return true;
        }
        for (int i = 1; i <= tryCount; i++) { // 高并发场景下，可以看到 i 处于 1 ~ 3 之间
            // 如果文件已存在，也会返回 false，所以该值不能作为是否能创建的依据，因此不对其进行处理
            // noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            if (dir.exists()) {
                return true;
            }
            ThreadKit.sleep(sleepMillis);
        }
        return dir.exists();
    }

    /**
     * 创建临时文件 创建后的文件名为 prefix[Random].tmp
     *
     * @param dir 临时文件创建的所在目录
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(final File dir) throws InternalException {
        return createTempFile("x", null, dir, true);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Random].tmp。 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定。 在 UNIX 系统上，此属性的默认值通常是
     * {@code "tmp"} 或 {@code "vartmp"}； 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}。 调用 Java
     * 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响。
     *
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile() throws InternalException {
        return createTempFile("x", null, null, true);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Random].suffix。 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定。 在 UNIX 系统上，此属性的默认值通常是
     * {@code "tmp"} 或 {@code "vartmp"}； 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}。 调用 Java
     * 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响。
     *
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(final String suffix, final boolean isReCreat) throws InternalException {
        return createTempFile("x", suffix, null, isReCreat);
    }

    /**
     * 在默认临时文件目录下创建临时文件，创建后的文件名为 prefix[Random].suffix。 默认临时文件目录由系统属性 {@code java.io.tmpdir} 指定。 在 UNIX 系统上，此属性的默认值通常是
     * {@code "tmp"} 或 {@code "vartmp"}； 在 Microsoft Windows 系统上，它通常是 {@code "C:\\WINNT\\TEMP"}。 调用 Java
     * 虚拟机时，可以为该系统属性赋予不同的值，但不保证对该属性的编程更改对该方法使用的临时目录有任何影响。
     *
     * @param prefix    前缀，至少3个字符
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(final String prefix, final String suffix, final boolean isReCreat)
            throws InternalException {
        return createTempFile(prefix, suffix, null, isReCreat);
    }

    /**
     * 创建临时文件 创建后的文件名为 prefix[Random].tmp
     *
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(final File dir, final boolean isReCreat) throws InternalException {
        return createTempFile("x", null, dir, isReCreat);
    }

    /**
     * 创建临时文件
     *
     * @param prefix    前缀，至少3个字符
     * @param suffix    后缀，如果null则使用默认.tmp
     * @param dir       临时文件创建的所在目录
     * @param isReCreat 是否重新创建文件（删掉原来的，创建新的）
     * @return 临时文件
     * @throws InternalException IO异常
     */
    public static File createTempFile(final String prefix, final String suffix, final File dir, final boolean isReCreat)
            throws InternalException {
        try {
            final File file = PathResolve.createTempFile(prefix, suffix, null == dir ? null : dir.toPath()).toFile()
                    .getCanonicalFile();
            if (isReCreat) {
                // noinspection ResultOfMethodCallIgnored
                file.delete();
                // noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            return file;
        } catch (final IOException e) { // fixes java.io.WinNTFileSystem.createFileExclusively access denied
            throw new InternalException(e);
        }
    }

    /**
     * 拷贝资源到目标文件
     * <ul>
     * <li>如果src为{@link FileResource}，调用文件拷贝。</li>
     * <li>其它，调用JDK7+的 {@link Files#copy(InputStream, Path, CopyOption...)}。</li>
     * </ul>
     *
     * @param src        源文件
     * @param target     目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException IO异常
     */
    public static File copy(final Resource src, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copy(src, target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * 通过JDK7+的 Files#copier(InputStream, Path, CopyOption...) 方法拷贝文件
     *
     * @param src     源文件流，使用后不关闭
     * @param target  目标文件
     * @param options {@link StandardCopyOption}
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static File copy(final InputStream src, final File target, final StandardCopyOption... options)
            throws InternalException {
        // check
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(target, "Target File or directory is null !");
        return copy(src, target.toPath(), options).toFile();
    }

    /**
     * 将文件写入流中，此方法不会关闭输出流
     *
     * @param src 文件
     * @param out 流
     * @return 写出的流byte数
     * @throws InternalException IO异常
     */
    public static long copy(final File src, final OutputStream out) throws InternalException {
        // check
        Assert.notNull(src, "Source File is null !");
        Assert.notNull(out, "Target stream is null !");
        return copy(src.toPath(), out);
    }

    /**
     * 复制文件或目录 如果目标文件为目录，则将源文件以相同文件名拷贝到目标目录
     *
     * @param srcPath    源文件或目录
     * @param targetPath 目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException IO异常
     */
    public static File copy(final String srcPath, final String targetPath, final boolean isOverride)
            throws InternalException {
        return copy(file(srcPath), file(targetPath), isOverride);
    }

    /**
     * 复制文件或目录 情况如下：
     *
     * <pre>
     * 1、src和dest都为目录，则将src目录及其目录下所有文件目录拷贝到dest下
     * 2、src和dest都为文件，直接复制，名字为dest
     * 3、src为文件，dest为目录，将src拷贝到dest目录下
     * </pre>
     *
     * @param src        源文件
     * @param target     目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException IO异常
     */
    public static File copy(final File src, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copy(src.toPath(), target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * 复制文件或目录 情况如下：
     *
     * <pre>
     * 1、src和dest都为目录，则将src下所有文件目录拷贝到dest下
     * 2、src和dest都为文件，直接复制，名字为dest
     * 3、src为文件，dest为目录，将src拷贝到dest目录下
     * </pre>
     *
     * @param src        源文件
     * @param target     目标文件或目录，目标不存在会自动创建（目录、文件都创建）
     * @param isOverride 是否覆盖目标文件
     * @return 目标目录或文件
     * @throws InternalException IO异常
     */
    public static File copyContent(final File src, final File target, final boolean isOverride)
            throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return copyContent(src.toPath(), target.toPath(),
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {}).toFile();
    }

    /**
     * 移动文件或目录到目标中，例如：
     * <ul>
     * <li>如果src为文件，target为目录，则移动到目标目录下，存在同名文件则按照是否覆盖参数执行。</li>
     * <li>如果src为文件，target为文件，则按照是否覆盖参数执行。</li>
     * <li>如果src为文件，target为不存在的路径，则重命名源文件到目标指定的文件，如moveContent("/a/b", "/c/d"), d不存在，则b变成d。</li>
     * <li>如果src为目录，target为文件，抛出{@link IllegalArgumentException}</li>
     * <li>如果src为目录，target为目录，则将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     * <li>如果src为目录，target为不存在的路径，则创建目标路径为目录，将源目录及其内容移动到目标路径目录中，如move("/a/b", "/c/d")，结果为"/c/d/b"</li>
     * </ul>
     *
     * @param src        源文件或目录路径
     * @param target     目标路径，如果为目录，则移动到此目录下
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件或目录
     * @throws InternalException IO异常
     * @see PathResolve#move(Path, Path, boolean)
     */
    public static File move(final File src, final File target, final boolean isOverride) throws InternalException {
        Assert.notNull(src, "Src file must be not null!");
        Assert.notNull(target, "target file must be not null!");
        return move(src.toPath(), target.toPath(), isOverride).toFile();
    }

    /**
     * 修改文件或目录的文件名，不变更路径，只是简单修改文件名，不保留扩展名。
     *
     * <pre>
     * FileKit.rename(file, "aaa.png", true) xx/xx.png = xx/aaa.png
     * </pre>
     *
     * @param file       被修改的文件
     * @param newName    新的文件名，如需扩展名，需自行在此参数加上，原文件名的扩展名不会被保留
     * @param isOverride 是否覆盖目标文件
     * @return 目标文件
     */
    public static File rename(final File file, final String newName, final boolean isOverride) {
        return rename(file, newName, false, isOverride);
    }

    /**
     * 修改文件或目录的文件名，不变更路径，只是简单修改文件名 重命名有两种模式： 1、isRetainExt为true时，保留原扩展名：
     *
     * <pre>
     * FileKit.rename(file, "aaa", true) xx/xx.png = xx/aaa.png
     * </pre>
     * 
     * 2、isRetainExt为false时，不保留原扩展名，需要在newName中
     *
     * <pre>
     * FileKit.rename(file, "aaa.jpg", false) xx/xx.png = xx/aaa.jpg
     * </pre>
     *
     * @param file        被修改的文件
     * @param newName     新的文件名，可选是否包括扩展名
     * @param isRetainExt 是否保留原文件的扩展名，如果保留，则newName不需要加扩展名
     * @param isOverride  是否覆盖目标文件
     * @return 目标文件
     * @see PathResolve#rename(Path, String, boolean)
     */
    public static File rename(final File file, String newName, final boolean isRetainExt, final boolean isOverride) {
        if (isRetainExt) {
            final String extName = FileName.extName(file);
            if (StringKit.isNotBlank(extName)) {
                newName = newName.concat(".").concat(extName);
            }
        }
        return rename(file.toPath(), newName, isOverride).toFile();
    }

    /**
     * 获取规范的绝对路径
     *
     * @param file 文件
     * @return 规范绝对路径，如果传入file为null，返回null
     */
    public static String getCanonicalPath(final File file) {
        if (null == file) {
            return null;
        }
        try {
            return file.getCanonicalPath();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取绝对路径 此方法不会判定给定路径是否有效（文件或目录存在）
     *
     * @param path      相对路径
     * @param baseClass 相对路径所相对的类
     * @return 绝对路径
     */
    public static String getAbsolutePath(final String path, final Class<?> baseClass) {
        final String normalPath;
        if (path == null) {
            normalPath = Normal.EMPTY;
        } else {
            normalPath = FileName.normalize(path);
            if (isAbsolutePath(normalPath)) {
                // 给定的路径已经是绝对路径了
                return normalPath;
            }
        }

        // 相对于ClassPath路径
        final URL url = ResourceKit.getResourceUrl(normalPath, baseClass);
        if (null != url) {
            // 对于jar中文件包含file:前缀，需要去掉此类前缀，在此做标准化，解决中文或空格路径被编码的问题
            return FileName.normalize(UrlKit.getDecodedPath(url));
        }

        // 如果资源不存在，则返回一个拼接的资源绝对路径
        final String classPath = ClassKit.getClassPath();
        if (null == classPath) {
            // throw new NullPointerException("ClassPath is null !");
            // 在jar运行模式中，ClassPath有可能获取不到，此时返回原始相对路径（此时获取的文件为相对工作目录）
            return normalPath;
        }

        // 资源不存在的情况下使用标准化路径有问题，使用原始路径拼接后标准化路径
        return FileName.normalize(classPath.concat(Objects.requireNonNull(normalPath)));
    }

    /**
     * 获取绝对路径，相对于ClassPath的目录 如果给定就是绝对路径，则返回原路径，原路径把所有\替换为/ 兼容Spring风格的路径表示，例如：classpath:config/example.setting也会被识别后转换
     *
     * @param path 相对路径
     * @return 绝对路径
     */
    public static String getAbsolutePath(final String path) {
        return getAbsolutePath(path, null);
    }

    /**
     * 获取标准的绝对路径
     *
     * @param file 文件
     * @return 绝对路径
     */
    public static String getAbsolutePath(final File file) {
        if (file == null) {
            return null;
        }

        try {
            return file.getCanonicalPath();
        } catch (final IOException e) {
            return file.getAbsolutePath();
        }
    }

    /**
     * 给定路径已经是绝对路径 此方法并没有针对路径做标准化，建议先执行{@link FileName#normalize(String)}方法标准化路径后判断 绝对路径判断条件是：
     * <ul>
     * <li>以/开头的路径</li>
     * <li>满足类似于 c:/xxxxx，其中祖母随意，不区分大小写</li>
     * <li>满足类似于 d:\xxxxx，其中祖母随意，不区分大小写</li>
     * </ul>
     *
     * @param path 需要检查的Path
     * @return 是否已经是绝对路径
     */
    public static boolean isAbsolutePath(final String path) {
        if (StringKit.isEmpty(path)) {
            return false;
        }

        // 给定的路径已经是绝对路径了
        return Symbol.C_SLASH == path.charAt(0) || PatternKit.isMatch(PATTERN_PATH_ABSOLUTE, path)
                || path.startsWith(Symbol.BACKSLASH + Symbol.BACKSLASH);
    }

    /**
     * 判断是否为目录，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果为目录true
     */
    public static boolean isDirectory(final String path) {
        return (null != path) && file(path).isDirectory();
    }

    /**
     * 判断是否为目录，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为目录true
     */
    public static boolean isDirectory(final File file) {
        return (null != file) && file.isDirectory();
    }

    /**
     * 判断是否为文件，如果path为null，则返回false
     *
     * @param path 文件路径
     * @return 如果为文件true
     */
    public static boolean isFile(final String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * 判断是否为文件，如果file为null，则返回false
     *
     * @param file 文件
     * @return 如果为文件true
     */
    public static boolean isFile(final File file) {
        return (null != file) && file.isFile();
    }

    /**
     * 检查两个文件是否是同一个文件 所谓文件相同，是指File对象是否指向同一个文件或文件夹，规则为：
     * <ul>
     * <li>当两个文件都为{@code null}时，返回{@code true}</li>
     * <li>当两个文件都存在时，检查是否为同一个文件</li>
     * <li>当两个文件都不存在时，检查路径是否一致</li>
     * </ul>
     *
     * @param file1 文件1，可以为{@code null}
     * @param file2 文件2，可以为{@code null}
     * @return 是否相同
     */
    public static boolean equals(final File file1, final File file2) {
        // 两者都为null判定为相同
        if (null == file1 || null == file2) {
            return null == file1 && null == file2;
        }

        final boolean exists1 = file1.exists();
        final boolean exists2 = file2.exists();

        // 当两个文件都存在时，检查是否为同一个文件
        if (exists1 && exists2) {
            return PathResolve.isSameFile(file1.toPath(), file2.toPath());
        }

        // 都不存在时，检查路径是否相同
        if (!exists1 && !exists2) {
            return pathEquals(file1, file2);
        }

        return false;
    }

    /**
     * 比较两个文件内容是否相同 首先比较长度，长度一致再比较内容 此方法来自Apache Commons io
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 两个文件内容一致返回true，否则false
     * @throws InternalException IO异常
     */
    public static boolean contentEquals(final File file1, final File file2) throws InternalException {
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // 两个文件都不存在，返回true
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // 不比较目录
            throw new InternalException("Can't compare directories, only files");
        }

        if (file1.length() != file2.length()) {
            // 文件长度不同
            return false;
        }

        if (equals(file1, file2)) {
            // 同一个文件
            return true;
        }

        InputStream input1 = null;
        InputStream input2 = null;
        try {
            input1 = getInputStream(file1);
            input2 = getInputStream(file2);
            return IoKit.contentEquals(input1, input2);

        } finally {
            IoKit.closeQuietly(input1);
            IoKit.closeQuietly(input2);
        }
    }

    /**
     * 比较两个文件内容是否相同 首先比较长度，长度一致再比较内容，比较内容采用按行读取，每行比较 此方法来自Apache Commons io
     *
     * @param file1   文件1
     * @param file2   文件2
     * @param charset 编码，null表示使用平台默认编码 两个文件内容一致返回true，否则false
     * @return 是否相同
     * @throws InternalException IO异常
     */
    public static boolean contentEqualsIgnoreEOL(final File file1, final File file2,
            final java.nio.charset.Charset charset) throws InternalException {
        final boolean file1Exists = file1.exists();
        if (file1Exists != file2.exists()) {
            return false;
        }

        if (!file1Exists) {
            // 两个文件都不存在，返回true
            return true;
        }

        if (file1.isDirectory() || file2.isDirectory()) {
            // 不比较目录
            throw new InternalException("Can't compare directories, only files");
        }

        if (equals(file1, file2)) {
            // 同一个文件
            return true;
        }

        Reader input1 = null;
        Reader input2 = null;
        try {
            input1 = getReader(file1, charset);
            input2 = getReader(file2, charset);
            return IoKit.contentEqualsIgnoreEOL(input1, input2);
        } finally {
            IoKit.closeQuietly(input1);
            IoKit.closeQuietly(input2);
        }
    }

    /**
     * 文件路径是否相同 取两个文件的绝对路径比较，在Windows下忽略大小写，在Linux下不忽略。
     *
     * @param file1 文件1
     * @param file2 文件2
     * @return 文件路径是否相同
     */
    public static boolean pathEquals(final File file1, final File file2) {
        if (isWindows()) {
            // Windows环境
            try {
                if (StringKit.equalsIgnoreCase(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (final Exception e) {
                if (StringKit.equalsIgnoreCase(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        } else {
            // 类Unix环境
            try {
                if (StringKit.equals(file1.getCanonicalPath(), file2.getCanonicalPath())) {
                    return true;
                }
            } catch (final Exception e) {
                if (StringKit.equals(file1.getAbsolutePath(), file2.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获得最后一个文件路径分隔符的位置
     *
     * @param filePath 文件路径
     * @return 最后一个文件路径分隔符的位置
     */
    public static int lastIndexOfSeparator(final String filePath) {
        if (StringKit.isNotEmpty(filePath)) {
            int i = filePath.length();
            char c;
            while (--i >= 0) {
                c = filePath.charAt(i);
                if (CharKit.isFileSeparator(c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 判断文件是否被改动 如果文件对象为 null 或者文件不存在，被视为改动
     *
     * @param file           文件对象
     * @param lastModifyTime 上次的改动时间
     * @return 是否被改动
     */
    public static boolean isModified(final File file, final long lastModifyTime) {
        if (null == file || !file.exists()) {
            return true;
        }
        return file.lastModified() != lastModifyTime;
    }

    /**
     * 修复路径 如果原路径尾部有分隔符，则保留为标准分隔符（/），否则不保留
     * <ol>
     * <li>1. 统一用 /</li>
     * <li>2. 多个 / 转换为一个 /</li>
     * <li>3. 去除左边空格</li>
     * <li>4. .. 和 . 转换为绝对路径，当..多于已有路径时，直接返回根路径</li>
     * <li>5. SMB路径保留，如\\127.0.0.0\a\b.zip</li>
     * </ol>
     * 
     * <pre>
     * "/foo//" = "/foo/"
     * "/foo/./" = "/foo/"
     * "/foo/../bar" = "/bar"
     * "/foo/../bar/" = "/bar/"
     * "/foo/../bar/../baz" = "/baz"
     * "/../" = "/"
     * "foo/bar/.." = "foo"
     * "foo/../bar" = "bar"
     * "foo/../../bar" = "bar"
     * "//server/foo/../bar" = "/server/bar"
     * "//server/../bar" = "/bar"
     * "C:\\foo\\..\\bar" = "C:/bar"
     * "C:\\..\\bar" = "C:/bar"
     * "~/foo/../bar/" = "~/bar/"
     * "~/../bar" = 普通用户运行是'bar的home目录'，ROOT用户运行是'/bar'
     * </pre>
     *
     * @param path 原路径
     * @return 修复后的路径
     */
    public static String normalize(final String path) {
        return FileName.normalize(path);
    }

    /**
     * 获得相对子路径
     * 
     * <pre>
     * dirPath: d:/aaa/bbb    filePath: d:/aaa/bbb/ccc         =    ccc
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/ccc.txt     =    ccc.txt
     * </pre>
     *
     * @param rootDir 绝对父路径
     * @param file    文件
     * @return 相对子路径
     */
    public static String subPath(final String rootDir, final File file) {
        try {
            return subPath(rootDir, file.getCanonicalPath());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获得相对子路径，忽略大小写
     * 
     * <pre>
     * dirPath: d:/aaa/bbb    filePath: d:/aaa/bbb/ccc        =    ccc
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/ccc.txt    =    ccc.txt
     * dirPath: d:/Aaa/bbb    filePath: d:/aaa/bbb/           =    ""
     * </pre>
     *
     * @param dirPath  父路径
     * @param filePath 文件路径
     * @return 相对子路径
     */
    public static String subPath(String dirPath, String filePath) {
        if (StringKit.isNotEmpty(dirPath) && StringKit.isNotEmpty(filePath)) {

            dirPath = StringKit.removeSuffix(FileName.normalize(dirPath), "/");
            filePath = FileName.normalize(filePath);

            final String result = StringKit.removePrefixIgnoreCase(filePath, dirPath);
            return StringKit.removePrefix(result, "/");
        }
        return filePath;
    }

    /**
     * 判断文件路径是否有指定后缀，忽略大小写 常用语判断扩展名
     *
     * @param file   文件或目录
     * @param suffix 后缀
     * @return 是否有指定后缀
     */
    public static boolean pathEndsWith(final File file, final String suffix) {
        return file.getPath().toLowerCase().endsWith(suffix);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * <pre>
     *      1、无法识别类型默认按照扩展名识别
     *      2、xls、doc、msi头信息无法区分，按照扩展名区分
     *      3、zip可能为docx、xlsx、pptx、jar、war头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param file 文件 {@link File}
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException IO异常
     * @see FileType#getType(File)
     */
    public static String getType(final File file) throws InternalException {
        return FileType.getType(file);
    }

    /**
     * 获得输入流
     *
     * @param file 文件
     * @return 输入流
     * @throws InternalException 文件未找到
     * @see IoKit#toStream(File)
     */
    public static BufferedInputStream getInputStream(final File file) throws InternalException {
        return IoKit.toBuffered(IoKit.toStream(file));
    }

    /**
     * 获得输入流
     *
     * @param path 文件路径
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BufferedInputStream getInputStream(final String path) throws InternalException {
        return getInputStream(file(path));
    }

    /**
     * 获得BOM输入流，用于处理带BOM头的文件
     *
     * @param file 文件
     * @return 输入流
     * @throws InternalException 文件未找到
     */
    public static BOMInputStream getBOMInputStream(final File file) throws InternalException {
        try {
            return new BOMInputStream(Files.newInputStream(file.toPath()));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 读取带BOM头的文件为Reader
     *
     * @param file 文件
     * @return BufferedReader对象
     */
    public static BomReader getBOMReader(final File file) {
        return IoKit.toBomReader(getBOMInputStream(file));
    }

    /**
     * 获得一个文件读取器
     *
     * @param file 文件
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedReader getUtf8Reader(final File file) throws InternalException {
        return getReader(file, Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path 文件路径
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedReader getUtf8Reader(final String path) throws InternalException {
        return getReader(path, Charset.UTF_8);
    }

    /**
     * 获得一个文件读取器
     *
     * @param file    文件
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedReader getReader(final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return IoKit.toReader(getInputStream(file), charset);
    }

    /**
     * 获得一个文件读取器
     *
     * @param path    绝对路径
     * @param charset 字符集
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedReader getReader(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return getReader(file(path), charset);
    }

    /**
     * 读取文件所有数据 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param file 文件
     * @return 字节码
     * @throws InternalException IO异常
     */
    public static byte[] readBytes(final File file) throws InternalException {
        if (null == file) {
            return null;
        }
        return readBytes(file.toPath());
    }

    /**
     * 读取文件所有数据 文件的长度不能超过Integer.MAX_VALUE
     *
     * @param filePath 文件路径
     * @return 字节码
     * @throws InternalException IO异常
     */
    public static byte[] readBytes(final String filePath) throws InternalException {
        return readBytes(file(filePath));
    }

    /**
     * 读取文件内容
     *
     * @param file 文件
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readUtf8String(final File file) throws InternalException {
        return readString(file, Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readUtf8String(final String path) throws InternalException {
        return readString(path, Charset.UTF_8);
    }

    /**
     * 读取文件内容
     *
     * @param file    文件
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readString(final File file, final java.nio.charset.Charset charset) throws InternalException {
        return FileReader.of(file, charset).readString();
    }

    /**
     * 读取文件内容
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readString(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return readString(file(path), charset);
    }

    /**
     * 读取文件内容
     *
     * @param url     文件URL
     * @param charset 字符集
     * @return 内容
     * @throws InternalException IO异常
     */
    public static String readString(final URL url, final java.nio.charset.Charset charset) throws InternalException {
        Assert.notNull(url, "Empty url provided!");

        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.read(in, charset);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 从文件中读取每一行的UTF-8编码数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(final String path, final T collection)
            throws InternalException {
        return readLines(path, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param path       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readLines(final String path, final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        return readLines(file(path), charset, collection);
    }

    /**
     * 从文件中读取每一行数据，数据编码为UTF-8
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(final File file, final T collection)
            throws InternalException {
        return readLines(file, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param file       文件路径
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readLines(final File file, final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        return FileReader.of(file, charset).readLines(collection);
    }

    /**
     * 从文件中读取每一行数据，编码为UTF-8
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readUtf8Lines(final URL url, final T collection)
            throws InternalException {
        return readLines(url, Charset.UTF_8, collection);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param <T>        集合类型
     * @param url        文件的URL
     * @param charset    字符集
     * @param collection 集合
     * @return 文件中的每行内容的集合
     * @throws InternalException IO异常
     */
    public static <T extends Collection<String>> T readLines(final URL url, final java.nio.charset.Charset charset,
            final T collection) throws InternalException {
        InputStream in = null;
        try {
            in = url.openStream();
            return IoKit.readLines(in, charset, collection);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url 文件的URL
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readUtf8Lines(final URL url) throws InternalException {
        return readLines(url, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param url     文件的URL
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readLines(final URL url, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(url, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据，编码为UTF-8
     *
     * @param path 文件路径
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readUtf8Lines(final String path) throws InternalException {
        return readLines(path, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param path    文件路径
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readLines(final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(path, charset, new ArrayList<>());
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file 文件
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readUtf8Lines(final File file) throws InternalException {
        return readLines(file, Charset.UTF_8);
    }

    /**
     * 从文件中读取每一行数据
     *
     * @param file    文件
     * @param charset 字符集
     * @return 文件中的每行内容的集合List
     * @throws InternalException IO异常
     */
    public static List<String> readLines(final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return readLines(file, charset, new ArrayList<>());
    }

    /**
     * 按行处理文件内容，编码为UTF-8
     *
     * @param file        文件
     * @param lineHandler {@link ConsumerX}行处理器
     * @throws InternalException IO异常
     */
    public static void readUtf8Lines(final File file, final ConsumerX<String> lineHandler) throws InternalException {
        readLines(file, Charset.UTF_8, lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        文件
     * @param charset     编码
     * @param lineHandler {@link ConsumerX}行处理器
     * @throws InternalException IO异常
     */
    public static void readLines(final File file, final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) throws InternalException {
        FileReader.of(file, charset).readLines(lineHandler);
    }

    /**
     * 按行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link ConsumerX}行处理器
     * @throws InternalException IO异常
     */
    public static void readLines(final RandomAccessFile file, final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) {
        String line;
        try {
            while ((line = file.readLine()) != null) {
                lineHandler.accept(Charset.convert(line, Charset.ISO_8859_1, charset));
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file        {@link RandomAccessFile}文件
     * @param charset     编码
     * @param lineHandler {@link ConsumerX}行处理器
     * @throws InternalException IO异常
     */
    public static void readLine(final RandomAccessFile file, final java.nio.charset.Charset charset,
            final ConsumerX<String> lineHandler) {
        final String line = readLine(file, charset);
        if (null != line) {
            lineHandler.accept(line);
        }
    }

    /**
     * 单行处理文件内容
     *
     * @param file    {@link RandomAccessFile}文件
     * @param charset 编码
     * @return 行内容
     * @throws InternalException IO异常
     */
    public static String readLine(final RandomAccessFile file, final java.nio.charset.Charset charset) {
        final String line;
        try {
            line = file.readLine();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (null != line) {
            return Charset.convert(line, Charset.ISO_8859_1, charset);
        }

        return null;
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @return 从文件中load出的数据
     * @throws InternalException IO异常
     */
    public static <T> T readUtf8(final String path, final FunctionX<BufferedReader, T> readerHandler)
            throws InternalException {
        return read(path, Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param path          文件的绝对路径
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InternalException IO异常
     */
    public static <T> T read(final String path, final java.nio.charset.Charset charset,
            final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        return read(file(path), charset, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @return 从文件中load出的数据
     * @throws InternalException IO异常
     */
    public static <T> T readUtf8(final File file, final FunctionX<BufferedReader, T> readerHandler)
            throws InternalException {
        return read(file, Charset.UTF_8, readerHandler);
    }

    /**
     * 按照给定的readerHandler读取文件中的数据
     *
     * @param <T>           集合类型
     * @param readerHandler Reader处理类
     * @param file          文件
     * @param charset       字符集
     * @return 从文件中load出的数据
     * @throws InternalException IO异常
     */
    public static <T> T read(final File file, final java.nio.charset.Charset charset,
            final FunctionX<BufferedReader, T> readerHandler) throws InternalException {
        return FileReader.of(file, charset).read(readerHandler);
    }

    /**
     * 获得一个输出流对象
     *
     * @param file    文件
     * @param options 选项，如追加模式传{@link java.nio.file.StandardOpenOption#APPEND}
     * @return 输出流对象
     */
    public static BufferedOutputStream getOutputStream(final File file, final OpenOption... options) {
        return PathResolve.getOutputStream(touch(file).toPath(), options);
    }

    /**
     * 获得一个输出流对象
     *
     * @param path 输出到的文件路径，绝对路径
     * @return 输出流对象
     * @throws InternalException IO异常
     */
    public static BufferedOutputStream getOutputStream(final String path) {
        return getOutputStream(touch(path));
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param path     输出路径，绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedWriter getWriter(final String path, final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return getWriter(touch(path), charset, isAppend);
    }

    /**
     * 获得一个带缓存的写入对象
     *
     * @param file     输出文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return BufferedReader对象
     * @throws InternalException IO异常
     */
    public static BufferedWriter getWriter(final File file, final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).getWriter(isAppend);
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param path     输出路径，绝对路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException IO异常
     */
    public static PrintWriter getPrintWriter(final String path, final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(path, charset, isAppend));
    }

    /**
     * 获得一个打印写入对象，可以有print
     *
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 打印对象
     * @throws InternalException IO异常
     */
    public static PrintWriter getPrintWriter(final File file, final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return new PrintWriter(getWriter(file, charset, isAppend));
    }

    /**
     * 获取当前系统的换行分隔符
     *
     * <pre>
     * Windows: \r\n
     * Mac: \r
     * Linux: \n
     * </pre>
     *
     * @return 换行符
     */
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    /**
     * 将String写入文件，覆盖模式，字符集为UTF-8
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File writeUtf8String(final String content, final String path) throws InternalException {
        return writeString(content, path, Charset.UTF_8);
    }

    /**
     * 将String写入文件，覆盖模式，字符集为UTF-8
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File writeUtf8String(final String content, final File file) throws InternalException {
        return writeString(content, file, Charset.UTF_8);
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File writeString(final String content, final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return writeString(content, touch(path), charset);
    }

    /**
     * 将String写入文件，覆盖模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 被写入的文件
     * @throws InternalException IO异常
     */
    public static File writeString(final String content, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return FileWriter.of(file, charset).write(content);
    }

    /**
     * 将String写入文件，UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File appendUtf8String(final String content, final String path) throws InternalException {
        return appendString(content, path, Charset.UTF_8);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @param path    文件路径
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File appendString(final String content, final String path, final java.nio.charset.Charset charset)
            throws InternalException {
        return appendString(content, touch(path), charset);
    }

    /**
     * 将String写入文件，UTF-8编码追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File appendUtf8String(final String content, final File file) throws InternalException {
        return appendString(content, file, Charset.UTF_8);
    }

    /**
     * 将String写入文件，追加模式
     *
     * @param content 写入的内容
     * @param file    文件
     * @param charset 字符集
     * @return 写入的文件
     * @throws InternalException IO异常
     */
    public static File appendString(final String content, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return FileWriter.of(file, charset).append(content);
    }

    /**
     * 将列表写入文件，覆盖模式，编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 绝对路径
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeUtf8Lines(final Collection<T> list, final String path) throws InternalException {
        return writeLines(list, path, Charset.UTF_8);
    }

    /**
     * 将列表写入文件，覆盖模式，编码为UTF-8
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 绝对路径
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeUtf8Lines(final Collection<T> list, final File file) throws InternalException {
        return writeLines(list, file, Charset.UTF_8);
    }

    /**
     * 将列表写入文件，覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, path, charset, false);
    }

    /**
     * 将列表写入文件，覆盖模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset)
            throws InternalException {
        return writeLines(list, file, charset, false);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param file 文件
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File appendUtf8Lines(final Collection<T> list, final File file) throws InternalException {
        return appendLines(list, file, Charset.UTF_8);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>  集合元素类型
     * @param list 列表
     * @param path 文件路径
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File appendUtf8Lines(final Collection<T> list, final String path) throws InternalException {
        return appendLines(list, path, Charset.UTF_8);
    }

    /**
     * 将列表写入文件，追加模式
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param path    绝对路径
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File appendLines(final Collection<T> list, final String path,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, path, charset, true);
    }

    /**
     * 将列表写入文件，追加模式，策略为：
     * <ul>
     * <li>当文件为空，从开头追加，尾部不加空行</li>
     * <li>当有内容，换行追加，尾部不加空行</li>
     * <li>当有内容，并末尾有空行，依旧换行追加</li>
     * </ul>
     *
     * @param <T>     集合元素类型
     * @param list    列表
     * @param file    文件
     * @param charset 字符集
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File appendLines(final Collection<T> list, final File file,
            final java.nio.charset.Charset charset) throws InternalException {
        return writeLines(list, file, charset, true);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param path     文件路径
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeLines(final Collection<T> list, final String path,
            final java.nio.charset.Charset charset, final boolean isAppend) throws InternalException {
        return writeLines(list, file(path), charset, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>      集合元素类型
     * @param list     列表
     * @param file     文件
     * @param charset  字符集
     * @param isAppend 是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).writeLines(list, isAppend);
    }

    /**
     * 将列表写入文件
     *
     * @param <T>                 集合元素类型
     * @param list                列表
     * @param file                文件
     * @param charset             字符集
     * @param isAppend            是否追加
     * @param appendLineSeparator 是否在末尾追加换行符
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static <T> File writeLines(final Collection<T> list, final File file, final java.nio.charset.Charset charset,
            final boolean isAppend, final boolean appendLineSeparator) throws InternalException {
        return FileWriter.of(file, charset).writeLines(list, null, isAppend, appendLineSeparator);
    }

    /**
     * 将Map写入文件，每个键值对为一行，一行中键与值之间使用kvSeparator分隔
     *
     * @param map         Map
     * @param file        文件
     * @param kvSeparator 键和值之间的分隔符，如果传入null使用默认分隔符" = "
     * @param isAppend    是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static File writeUtf8Map(final Map<?, ?> map, final File file, final String kvSeparator,
            final boolean isAppend) throws InternalException {
        return FileWriter.of(file, Charset.UTF_8).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * 将Map写入文件，每个键值对为一行，一行中键与值之间使用kvSeparator分隔
     *
     * @param map         Map
     * @param file        文件
     * @param charset     字符集编码
     * @param kvSeparator 键和值之间的分隔符，如果传入null使用默认分隔符" = "
     * @param isAppend    是否追加
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static File writeMap(final Map<?, ?> map, final File file, final java.nio.charset.Charset charset,
            final String kvSeparator, final boolean isAppend) throws InternalException {
        return FileWriter.of(file, charset).writeMap(map, kvSeparator, isAppend);
    }

    /**
     * 写数据到文件中 文件路径如果是相对路径，则相对ClassPath
     *
     * @param data 数据
     * @param path 相对ClassPath的目录或者绝对路径目录
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static File writeBytes(final byte[] data, final String path) throws InternalException {
        return writeBytes(data, touch(path));
    }

    /**
     * 写数据到文件中
     *
     * @param dest 目标文件
     * @param data 数据
     * @return 目标文件
     * @throws InternalException IO异常
     */
    public static File writeBytes(final byte[] data, final File dest) throws InternalException {
        return writeBytes(data, dest, 0, data.length, false);
    }

    /**
     * 写入数据到文件
     *
     * @param data     数据
     * @param target   目标文件
     * @param off      数据开始位置
     * @param len      数据长度
     * @param isAppend 是否追加模式
     * @return 目标文件
     */
    public static File writeBytes(final byte[] data, final File target, final int off, final int len,
            final boolean isAppend) {
        return FileWriter.of(target).write(data, off, len, isAppend);
    }

    /**
     * 将流的内容写入文件 此方法会自动关闭输入流
     *
     * @param target 目标文件
     * @param in     输入流
     * @return 目标文件
     */
    public static File writeFromStream(final InputStream in, final File target) {
        return writeFromStream(in, target, true);
    }

    /**
     * 将流的内容写入文件
     *
     * @param target    目标文件
     * @param in        输入流
     * @param isCloseIn 是否关闭输入流
     * @return 目标文件
     */
    public static File writeFromStream(final InputStream in, final File target, final boolean isCloseIn) {
        return FileWriter.of(target).writeFromStream(in, isCloseIn);
    }

    /**
     * 将文件写入流中，此方法不会关闭输出流
     *
     * @param file 文件
     * @param out  流
     * @return 写出的流byte数
     */
    public static long writeToStream(final File file, final OutputStream out) {
        return FileReader.of(file).writeToStream(out);
    }

    /**
     * 可读的文件大小
     *
     * @param file 文件
     * @return 大小
     */
    public static String readableFileSize(final File file) {
        Assert.notNull(file);
        return readableFileSize(file.length());
    }

    /**
     * 可读的文件大小 参考 <a href=
     * "http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc">http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc</a>
     *
     * @param size Long类型大小
     * @return 大小
     * @see DataSize#format(long)
     */
    public static String readableFileSize(final long size) {
        return DataSize.format(size);
    }

    /**
     * 转换文件编码 此方法用于转换文件编码，读取的文件实际编码必须与指定的srcCharset编码一致，否则导致乱码
     *
     * @param file        文件
     * @param srcCharset  原文件的编码，必须与文件内容的编码保持一致
     * @param destCharset 转码后的编码
     * @return 被转换编码的文件
     * @see Charset#convert(File, java.nio.charset.Charset, java.nio.charset.Charset)
     */
    public static File convertCharset(final File file, final java.nio.charset.Charset srcCharset,
            final java.nio.charset.Charset destCharset) {
        return Charset.convert(file, srcCharset, destCharset);
    }

    /**
     * 转换换行符 将给定文件的换行符转换为指定换行符
     *
     * @param file          文件
     * @param charset       编码
     * @param lineSeparator 换行符枚举{@link LineSeparator}
     * @return 被修改的文件
     */
    public static File convertLineSeparator(final File file, final java.nio.charset.Charset charset,
            final LineSeparator lineSeparator) {
        final List<String> lines = readLines(file, charset);
        return FileWriter.of(file, charset).writeLines(lines, lineSeparator, false);
    }

    /**
     * 获取Web项目下的web root路径 原理是首先获取ClassPath路径，由于在web项目中ClassPath位于 WEB-INF/classes/下，故向上获取两级目录即可。
     *
     * @return web root路径
     */
    public static File getWebRoot() {
        final String classPath = ClassKit.getClassPath();
        Console.log(classPath);
        if (StringKit.isNotBlank(classPath)) {
            return getParent(file(classPath), 2);
        }
        return null;
    }

    /**
     * 获取指定文件的父路径
     *
     * <pre>
     * getParent(file("d:/aaa/bbb/cc/ddd")) - "d:/aaa/bbb/cc"
     * </pre>
     *
     * @param file 目录或文件
     * @return 路径File，如果不存在返回null
     */
    public static File getParent(final File file) {
        return getParent(file, 1);
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent("d:/aaa/bbb/cc/ddd", 0) - "d:/aaa/bbb/cc/ddd"
     * getParent("d:/aaa/bbb/cc/ddd", 2) - "d:/aaa/bbb"
     * getParent("d:/aaa/bbb/cc/ddd", 4) - "d:/"
     * getParent("d:/aaa/bbb/cc/ddd", 5) - null
     * </pre>
     *
     * @param filePath 目录或文件路径
     * @param level    层级
     * @return 路径File，如果不存在返回null
     */
    public static String getParent(final String filePath, final int level) {
        final File parent = getParent(file(filePath), level);
        try {
            return null == parent ? null : parent.getCanonicalPath();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取指定层级的父路径
     *
     * <pre>
     * getParent(file("d:/aaa/bbb/cc/ddd", 0)) - "d:/aaa/bbb/cc/ddd"
     * getParent(file("d:/aaa/bbb/cc/ddd", 2)) - "d:/aaa/bbb"
     * getParent(file("d:/aaa/bbb/cc/ddd", 4)) - "d:/"
     * getParent(file("d:/aaa/bbb/cc/ddd", 5)) - null
     * </pre>
     *
     * @param file  目录或文件
     * @param level 层级
     * @return 路径File，如果不存在返回null
     */
    public static File getParent(final File file, final int level) {
        if (level < 1 || null == file) {
            return file;
        }

        final File parentFile;
        try {
            parentFile = file.getCanonicalFile().getParentFile();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        if (1 == level) {
            return parentFile;
        }
        return getParent(parentFile, level - 1);
    }

    /**
     * 检查父完整路径是否为自路径的前半部分，如果不是说明不是子路径，可能存在slip注入。 见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parentFile 父文件或目录
     * @param file       子文件或目录
     * @return 子文件或目录
     * @throws IllegalArgumentException 检查创建的子文件不在父目录中抛出此异常
     */
    public static File checkSlip(final File parentFile, final File file) throws IllegalArgumentException {
        if (null != parentFile && null != file) {
            if (!isSub(parentFile, file)) {
                throw new IllegalArgumentException(
                        StringKit.format("New file [{}] is outside of the parent dir: [{}]", file, parentFile));
            }
        }
        return file;
    }

    /**
     * 根据文件扩展名获得MimeType
     *
     * @param filePath     文件路径或文件名
     * @param defaultValue 当获取MimeType为null时的默认值
     * @return MimeType
     */
    public static String getMimeType(final String filePath, final String defaultValue) {
        return ObjectKit.defaultIfNull(getMimeType(filePath), defaultValue);
    }

    /**
     * 根据文件扩展名获得MimeType
     *
     * @param filePath 文件路径或文件名
     * @return MimeType
     */
    public static String getMimeType(final String filePath) {
        if (StringKit.isBlank(filePath)) {
            return null;
        }

        // 补充一些常用的mimeType
        if (StringKit.endWithIgnoreCase(filePath, ".css")) {
            return "text/css";
        } else if (StringKit.endWithIgnoreCase(filePath, ".js")) {
            return "application/x-javascript";
        } else if (StringKit.endWithIgnoreCase(filePath, ".rar")) {
            return "application/x-rar-compressed";
        } else if (StringKit.endWithIgnoreCase(filePath, ".7z")) {
            return "application/x-7z-compressed";
        } else if (StringKit.endWithIgnoreCase(filePath, ".wgt")) {
            return "application/widget";
        } else if (StringKit.endWithIgnoreCase(filePath, ".webp")) {
            return "image/webp";
        }

        String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
        if (null == contentType) {
            contentType = getMimeType(Paths.get(filePath));
        }

        return contentType;
    }

    /**
     * 判断是否为符号链接文件
     *
     * @param file 被检查的文件
     * @return 是否为符号链接文件
     */
    public static boolean isSymlink(final File file) {
        return isSymlink(file.toPath());
    }

    /**
     * 判断给定的目录是否为给定文件或文件夹的子目录
     *
     * @param parent 父目录，非空
     * @param sub    子目录，非空
     * @return 子目录是否为父目录的子目录
     */
    public static boolean isSub(final File parent, final File sub) {
        Assert.notNull(parent);
        Assert.notNull(sub);
        return isSub(parent.toPath(), sub.toPath());
    }

    /**
     * 创建{@link RandomAccessFile}
     *
     * @param path 文件Path
     * @param mode 模式，见{@link FileMode}
     * @return {@link RandomAccessFile}
     */
    public static RandomAccessFile createRandomAccessFile(final Path path, final FileMode mode) {
        return createRandomAccessFile(path.toFile(), mode);
    }

    /**
     * 创建{@link RandomAccessFile}
     *
     * @param file 文件
     * @param mode 模式，见{@link FileMode}
     * @return {@link RandomAccessFile}
     */
    public static RandomAccessFile createRandomAccessFile(final File file, final FileMode mode) {
        try {
            return new RandomAccessFile(file, mode.name());
        } catch (final FileNotFoundException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param handler 行处理器
     */
    public static void tail(final File file, final ConsumerX<String> handler) {
        tail(file, Charset.UTF_8, handler);
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param charset 编码
     * @param handler 行处理器
     */
    public static void tail(final File file, final java.nio.charset.Charset charset, final ConsumerX<String> handler) {
        new FileTailer(file, charset, handler).start();
    }

    /**
     * 文件内容跟随器，实现类似Linux下"tail -f"命令功能 此方法会阻塞当前线程
     *
     * @param file    文件
     * @param charset 编码
     */
    public static void tail(final File file, final java.nio.charset.Charset charset) {
        tail(file, charset, FileTailer.CONSOLE_HANDLER);
    }

    /**
     * 创建 {@link FileSystem}
     *
     * @param path 文件路径，可以是目录或Zip文件等
     * @return {@link java.nio.file.FileSystem}
     */
    public static java.nio.file.FileSystem of(final String path) {
        try {
            return FileSystems.newFileSystem(Paths.get(path).toUri(), MapKit.of("create", "true"));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建 Zip的{@link java.nio.file.FileSystem}，默认UTF-8编码
     *
     * @param path 文件路径，可以是目录或Zip文件等
     * @return {@link java.nio.file.FileSystem}
     */
    public static java.nio.file.FileSystem createZip(final String path) {
        return createZip(path, null);
    }

    /**
     * 创建 Zip的{@link java.nio.file.FileSystem}
     *
     * @param path    文件路径，可以是目录或Zip文件等
     * @param charset 编码
     * @return {@link java.nio.file.FileSystem}
     */
    public static java.nio.file.FileSystem createZip(final String path, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = Charset.UTF_8;
        }
        final HashMap<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", charset.name());

        try {
            return FileSystems.newFileSystem(URI.create("jar:" + Paths.get(path).toUri()), env);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取目录的根路径，或Zip文件中的根路径
     *
     * @param fileSystem {@link java.nio.file.FileSystem}
     * @return 根 {@link Path}
     */
    public static Path getRoot(final FileSystem fileSystem) {
        return fileSystem.getPath(Symbol.SLASH);
    }

    /**
     * 根据压缩包中的路径构建目录结构，在Win下直接构建，在Linux下拆分路径单独构建
     *
     * @param outFile  最外部路径
     * @param fileName 文件名，可以包含路径
     * @return 文件或目录
     */
    private static File buildFile(File outFile, String fileName) {
        // 替换Windows路径分隔符为Linux路径分隔符，便于统一处理
        fileName = fileName.replace(Symbol.C_BACKSLASH, Symbol.C_SLASH);
        if (!isWindows()
                // 检查文件名中是否包含"/"，不考虑以"/"结尾的情况
                && fileName.lastIndexOf(Symbol.C_SLASH, fileName.length() - 2) > 0) {
            // 在Linux下多层目录创建存在问题，/会被当成文件名的一部分，此处做处理
            // 使用/拆分路径（zip中无\），级联创建父目录
            final List<String> pathParts = CharsBacker.split(fileName, Symbol.SLASH, false, true);
            final int lastPartIndex = pathParts.size() - 1;// 目录个数
            for (int i = 0; i < lastPartIndex; i++) {
                // 由于路径拆分，slip不检查，在最后一步检查
                outFile = new File(outFile, pathParts.get(i));
            }
            // noinspection ResultOfMethodCallIgnored
            outFile.mkdirs();
            // 最后一个部分如果非空，作为文件名
            fileName = pathParts.get(lastPartIndex);
        }
        return new File(outFile, fileName);
    }

}
