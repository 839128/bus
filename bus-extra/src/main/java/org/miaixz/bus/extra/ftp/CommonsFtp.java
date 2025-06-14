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
package org.miaixz.bus.extra.ftp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.miaixz.bus.core.io.file.FileName;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.extra.ssh.Connector;

/**
 * Apache Commons FTP客户端封装 此客户端基于Apache-Commons-Net 常见搭建ftp的工具有：
 * <ul>
 * <li>filezila server ;根目录一般都是空</li>
 * <li>linux vsftpd ; 使用的 系统用户的目录，这里往往都是不是根目录，如：/home/bus/ftp</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CommonsFtp extends AbstractFtp {

    /**
     * 默认端口
     */
    public static final int DEFAULT_PORT = 21;
    private FTPClient client;
    private EnumValue.FtpMode mode;
    /**
     * 执行完操作是否返回当前目录
     */
    private boolean backToPwd;

    /**
     * 构造
     *
     * @param config FTP配置
     * @param mode   模式
     */
    public CommonsFtp(final FtpConfig config, final EnumValue.FtpMode mode) {
        super(config);
        this.mode = mode;
        this.init();
    }

    /**
     * 构造
     *
     * @param client 自定义实例化好的{@link FTPClient}
     */
    public CommonsFtp(final FTPClient client) {
        super(FtpConfig.of());
        this.client = client;
    }

    /**
     * 构造CommonsFtp，匿名登录
     *
     * @param host 域名或IP
     * @return CommonsFtp
     */
    public static CommonsFtp of(final String host) {
        return of(host, DEFAULT_PORT);
    }

    /**
     * 构造，匿名登录
     *
     * @param host 域名或IP
     * @param port 端口
     * @return CommonsFtp
     */
    public static CommonsFtp of(final String host, final int port) {
        return of(host, port, "anonymous", Normal.EMPTY);
    }

    /**
     * 构造
     *
     * @param host     域名或IP
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @return CommonsFtp
     */
    public static CommonsFtp of(final String host, final int port, final String user, final String password) {
        return of(Connector.of(host, port, user, password), DEFAULT_CHARSET);
    }

    /**
     * 构造
     *
     * @param connector 连接信息，包括host、port、user、password等信息
     * @param charset   编码
     * @return CommonsFtp
     */
    public static CommonsFtp of(final Connector connector, final Charset charset) {
        return of(connector, charset, null, null);
    }

    /**
     * 构造
     *
     * @param connector          连接信息，包括host、port、user、password等信息
     * @param charset            编码
     * @param serverLanguageCode 服务器语言 例如：zh
     * @param systemKey          服务器标识 例如：org.apache.commons.net.ftp.FTPClientConfig.SYST_NT
     * @return CommonsFtp
     */
    public static CommonsFtp of(final Connector connector, final Charset charset, final String serverLanguageCode,
            final String systemKey) {
        return of(connector, charset, serverLanguageCode, systemKey, null);
    }

    /**
     * 构造
     *
     * @param connector          连接信息，包括host、port、user、password等信息
     * @param charset            编码
     * @param serverLanguageCode 服务器语言
     * @param systemKey          系统关键字
     * @param mode               模式
     * @return CommonsFtp
     */
    public static CommonsFtp of(final Connector connector, final Charset charset, final String serverLanguageCode,
            final String systemKey, final EnumValue.FtpMode mode) {
        return new CommonsFtp(new FtpConfig(connector, charset, serverLanguageCode, systemKey), mode);
    }

    /**
     * 初始化连接
     *
     * @return this
     */
    public CommonsFtp init() {
        return this.init(this.ftpConfig, this.mode);
    }

    /**
     * 初始化连接
     *
     * @param config FTP配置
     * @param mode   模式
     * @return this
     */
    public CommonsFtp init(final FtpConfig config, final EnumValue.FtpMode mode) {
        final FTPClient client = new FTPClient();
        client.setRemoteVerificationEnabled(false);

        final Charset charset = config.getCharset();
        if (null != charset) {
            client.setControlEncoding(charset.toString());
        }
        client.setConnectTimeout((int) config.getConnector().getTimeout());
        final String systemKey = config.getSystemKey();
        if (StringKit.isNotBlank(systemKey)) {
            final FTPClientConfig conf = new FTPClientConfig(systemKey);
            final String serverLanguageCode = config.getServerLanguageCode();
            if (StringKit.isNotBlank(serverLanguageCode)) {
                conf.setServerLanguageCode(config.getServerLanguageCode());
            }
            client.configure(conf);
        }

        // connect
        final Connector connector = config.getConnector();
        try {
            // 连接ftp服务器
            client.connect(connector.getHost(), connector.getPort());
            client.setSoTimeout((int) config.getSoTimeout());
            // 登录ftp服务器
            client.login(connector.getUser(), connector.getPassword());
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        final int replyCode = client.getReplyCode(); // 是否成功登录服务器
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            try {
                client.disconnect();
            } catch (final IOException e) {
                // ignore
            }
            throw new InternalException("Login failed for user [{}], reply code is: [{}]", connector.getUser(),
                    replyCode);
        }
        this.client = client;
        if (mode != null) {
            // noinspection resource
            setMode(mode);
        }
        return this;
    }

    /**
     * 设置FTP连接模式，可选主动和被动模式
     *
     * @param mode 模式枚举
     * @return this
     */
    public CommonsFtp setMode(final EnumValue.FtpMode mode) {
        this.mode = mode;
        switch (mode) {
        case Active:
            this.client.enterLocalActiveMode();
            break;
        case Passive:
            this.client.enterLocalPassiveMode();
            break;
        }
        return this;
    }

    /**
     * 是否执行完操作返回当前目录
     *
     * @return 执行完操作是否返回当前目录
     */
    public boolean isBackToPwd() {
        return this.backToPwd;
    }

    /**
     * 设置执行完操作是否返回当前目录
     *
     * @param backToPwd 执行完操作是否返回当前目录
     * @return this
     */
    public CommonsFtp setBackToPwd(final boolean backToPwd) {
        this.backToPwd = backToPwd;
        return this;
    }

    /**
     * 如果连接超时的话，重新进行连接 经测试，当连接超时时，client.isConnected()仍然返回ture，无法判断是否连接超时 因此，通过发送pwd命令的方式，检查连接是否超时
     *
     * @return this
     */
    @Override
    public CommonsFtp reconnectIfTimeout() {
        String pwd = null;
        try {
            pwd = pwd();
        } catch (final InternalException fex) {
            // ignore
        }

        if (pwd == null) {
            return this.init();
        }
        return this;
    }

    /**
     * 改变目录
     *
     * @param directory 目录
     * @return 是否成功
     */
    @Override
    synchronized public boolean cd(final String directory) {
        if (StringKit.isBlank(directory)) {
            // 当前目录
            return true;
        }

        try {
            return client.changeWorkingDirectory(directory);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 远程当前目录
     *
     * @return 远程当前目录
     */
    @Override
    public String pwd() {
        try {
            return client.printWorkingDirectory();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public List<String> ls(final String path) {
        return ArrayKit.mapToList(lsFiles(path), FTPFile::getName);
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历 此方法自动过滤"."和".."两种目录
     *
     * @param path      目录
     * @param predicate 过滤器，null表示不过滤，默认去掉"."和".."两种目录
     * @return 文件名或目录名列表
     */
    public List<String> ls(final String path, final Predicate<FTPFile> predicate) {
        return CollKit.map(lsFiles(path, predicate), FTPFile::getName);
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历 此方法自动过滤"."和".."两种目录
     *
     * @param path      目录
     * @param predicate 过滤器，null表示不过滤，默认去掉"."和".."两种目录
     * @return 文件或目录列表
     */
    public List<FTPFile> lsFiles(final String path, final Predicate<FTPFile> predicate) {
        final FTPFile[] ftpFiles = lsFiles(path);
        if (ArrayKit.isEmpty(ftpFiles)) {
            return ListKit.empty();
        }

        final List<FTPFile> result = new ArrayList<>(ftpFiles.length - 2 <= 0 ? ftpFiles.length : ftpFiles.length - 2);
        String fileName;
        for (final FTPFile ftpFile : ftpFiles) {
            fileName = ftpFile.getName();
            if (!StringKit.equals(".", fileName) && !StringKit.equals("..", fileName)) {
                if (null == predicate || predicate.test(ftpFile)) {
                    result.add(ftpFile);
                }
            }
        }
        return result;
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历
     *
     * @param path 目录，如果目录不存在，抛出异常
     * @return 文件或目录列表
     * @throws InternalException IO异常
     */
    public FTPFile[] lsFiles(final String path) throws InternalException {
        String pwd = null;
        if (StringKit.isNotBlank(path)) {
            pwd = pwd();
            if (!cd(path)) {
                throw new InternalException("Change dir to [{}] error, maybe path not exist!", path);
            }
        }

        FTPFile[] ftpFiles;
        try {
            ftpFiles = this.client.listFiles();
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            // 回到原目录
            cd(pwd);
        }

        return ftpFiles;
    }

    @Override
    public boolean rename(String oldPath, String newPath) {
        try {
            return this.client.rename(oldPath, newPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean mkdir(final String dir) throws InternalException {
        try {
            return this.client.makeDirectory(dir);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 获取服务端目录状态。
     *
     * @param path 路径
     * @return 状态int，服务端不同，返回不同
     * @throws InternalException IO异常
     */
    public int stat(final String path) throws InternalException {
        try {
            return this.client.stat(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 判断ftp服务器目录内是否还有子元素（目录或文件）
     *
     * @param path 文件路径
     * @return 是否存在
     * @throws InternalException IO异常
     */
    public boolean existFile(final String path) throws InternalException {
        final FTPFile[] ftpFileArr;
        try {
            ftpFileArr = client.listFiles(path);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return ArrayKit.isNotEmpty(ftpFileArr);
    }

    @Override
    public boolean delFile(final String path) throws InternalException {
        final String pwd = pwd();
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        if (!cd(dir)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", path);
        }

        boolean isSuccess;
        try {
            isSuccess = client.deleteFile(fileName);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            // 回到原目录
            cd(pwd);
        }
        return isSuccess;
    }

    @Override
    public boolean delDir(final String dirPath) throws InternalException {
        final FTPFile[] dirs;
        try {
            dirs = client.listFiles(dirPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        String name;
        String childPath;
        for (final FTPFile ftpFile : dirs) {
            name = ftpFile.getName();
            childPath = StringKit.format("{}/{}", dirPath, name);
            if (ftpFile.isDirectory()) {
                // 上级和本级目录除外
                if (!".".equals(name) && !"..".equals(name)) {
                    delDir(childPath);
                }
            } else {
                delFile(childPath);
            }
        }

        // 删除空目录
        try {
            return this.client.removeDirectory(dirPath);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. remotePath为null或""上传到当前路径
     * 2. remotePath为相对路径则相对于当前路径的子路径
     * 3. remotePath为绝对路径则上传到此路径
     * </pre>
     *
     * @param remotePath 服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param file       文件
     * @return 是否上传成功
     */
    @Override
    public boolean uploadFile(final String remotePath, final File file) {
        Assert.notNull(file, "file to upload is null !");
        if (!FileKit.isFile(file)) {
            throw new InternalException("[{}] is not a file!", file);
        }
        return uploadFile(remotePath, file.getName(), file);
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. remotePath为null或""上传到当前路径
     * 2. remotePath为相对路径则相对于当前路径的子路径
     * 3. remotePath为绝对路径则上传到此路径
     * </pre>
     *
     * @param file       文件
     * @param remotePath 服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param fileName   自定义在服务端保存的文件名
     * @return 是否上传成功
     * @throws InternalException IO异常
     */
    public boolean uploadFile(final String remotePath, final String fileName, final File file)
            throws InternalException {
        try (final InputStream in = FileKit.getInputStream(file)) {
            return uploadFile(remotePath, fileName, in);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. remotePath为null或""上传到当前路径
     * 2. remotePath为相对路径则相对于当前路径的子路径
     * 3. remotePath为绝对路径则上传到此路径
     * </pre>
     *
     * @param remotePath 服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 是否上传成功
     * @throws InternalException IO异常
     */
    public boolean uploadFile(final String remotePath, final String fileName, final InputStream fileStream)
            throws InternalException {
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (StringKit.isNotBlank(remotePath)) {
            mkDirs(remotePath);
            if (!cd(remotePath)) {
                throw new InternalException("Change dir to [{}] error, maybe dir not exist!", remotePath);
            }
        }

        try {
            return client.storeFile(fileName, fileStream);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (this.backToPwd) {
                cd(pwd);
            }
        }
    }

    /**
     * 递归上传文件（支持目录） 上传时，如果uploadFile为目录，只复制目录下所有目录和文件到目标路径下，并不会复制目录本身 上传时，自动创建父级目录
     *
     * @param remotePath 目录路径
     * @param uploadFile 上传文件或目录
     */
    public void upload(final String remotePath, final File uploadFile) {
        if (!FileKit.isDirectory(uploadFile)) {
            this.uploadFile(remotePath, uploadFile);
            return;
        }

        final File[] files = uploadFile.listFiles();
        if (ArrayKit.isEmpty(files)) {
            return;
        }

        final List<File> dirs = new ArrayList<>(files.length);
        // 第一次只处理文件，防止目录在前面导致先处理子目录，而引发文件所在目录不正确
        for (final File f : files) {
            if (f.isDirectory()) {
                dirs.add(f);
            } else {
                this.uploadFile(remotePath, f);
            }
        }
        // 第二次只处理目录
        for (final File f : dirs) {
            final String dir = FileKit.normalize(remotePath + "/" + f.getName());
            upload(dir, f);
        }
    }

    /**
     * 下载文件
     *
     * @param path    文件路径，包含文件名
     * @param outFile 输出文件或目录，当为目录时，使用服务端的文件名
     */
    @Override
    public void download(final String path, final File outFile) {
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        download(dir, fileName, outFile);
    }

    /**
     * 递归下载FTP服务器上文件到本地(文件目录和服务器同步)
     *
     * @param sourceDir ftp服务器目录
     * @param targetDir 本地目录
     */
    @Override
    public void recursiveDownloadFolder(final String sourceDir, final File targetDir) {
        String fileName;
        String srcFile;
        File destFile;
        for (final FTPFile ftpFile : lsFiles(sourceDir, null)) {
            fileName = ftpFile.getName();
            srcFile = StringKit.format("{}/{}", sourceDir, fileName);
            destFile = FileKit.file(targetDir, fileName);

            if (!ftpFile.isDirectory()) {
                // 本地不存在文件或者ftp上文件有修改则下载
                if (!FileKit.exists(destFile) || (ftpFile.getTimestamp().getTimeInMillis() > destFile.lastModified())) {
                    download(srcFile, destFile);
                }
            } else {
                // 服务端依旧是目录，继续递归
                FileKit.mkdir(destFile);
                recursiveDownloadFolder(srcFile, destFile);
            }
        }
    }

    /**
     * 下载文件
     *
     * @param path     文件所在路径（远程目录），不包含文件名
     * @param fileName 文件名
     * @param outFile  输出文件或目录，当为目录时使用服务端文件名
     * @throws InternalException IO异常
     * @return 是否下载成功
     */
    public boolean download(final String path, final String fileName, File outFile) throws InternalException {
        if (outFile.isDirectory()) {
            outFile = new File(outFile, fileName);
        }
        if (!outFile.exists()) {
            FileKit.touch(outFile);
        }
        try (final OutputStream out = FileKit.getOutputStream(outFile)) {
            return download(path, fileName, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 下载文件到输出流
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param out      输出位置
     * @return 是否下载成功
     */
    public boolean download(final String path, final String fileName, final OutputStream out) {
        return download(path, fileName, out, null);
    }

    /**
     * 下载文件到输出流
     *
     * @param path            服务端的文件路径
     * @param fileName        服务端的文件名
     * @param out             输出流，下载的文件写出到这个流中
     * @param fileNameCharset 文件名编码，通过此编码转换文件名编码为ISO8859-1
     * @throws InternalException IO异常
     * @return 是否下载成功
     */
    public boolean download(final String path, String fileName, final OutputStream out, final Charset fileNameCharset)
            throws InternalException {
        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (!cd(path)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", path);
        }

        if (null != fileNameCharset) {
            fileName = new String(fileName.getBytes(fileNameCharset), StandardCharsets.ISO_8859_1);
        }
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            return client.retrieveFile(fileName, out);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (backToPwd) {
                cd(pwd);
            }
        }
    }

    @Override
    public InputStream getFileStream(final String path) {
        final String fileName = FileName.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        return getFileStream(dir, fileName);
    }

    /**
     * 读取文件为输入流
     *
     * @param dir      服务端的文件目录
     * @param fileName 服务端的文件名
     * @return {@link InputStream}
     * @throws InternalException IO异常
     */
    public InputStream getFileStream(final String dir, final String fileName) throws InternalException {
        String pwd = null;
        if (isBackToPwd()) {
            pwd = pwd();
        }

        if (!cd(dir)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!", dir);
        }
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            return client.retrieveFileStream(fileName);
        } catch (final IOException e) {
            throw new InternalException(e);
        } finally {
            if (isBackToPwd()) {
                cd(pwd);
            }
        }
    }

    /**
     * 获取FTPClient客户端对象
     *
     * @return {@link FTPClient}
     */
    public FTPClient getClient() {
        return this.client;
    }

    @Override
    public void close() throws IOException {
        if (null != this.client) {
            this.client.logout();
            if (this.client.isConnected()) {
                this.client.disconnect();
            }
            this.client = null;
        }
    }

}
