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
package org.miaixz.bus.socket;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * 网络监控器，提供通讯层面监控功能的接口
 * <p>
 * 并未单独提供配置监控服务的接口，用户在使用时仅需在Handler实现类中同时实现当前Monitor接口即可。 在注册消息处理器时，若服务监测到该处理器同时实现了Monitor接口，则该监视器便会生效。
 * </p>
 * 
 * <pre>
 * public class MessageProcessorImpl implements Handler, Monitor {
 *
 * }
 * </pre>
 * 
 * 注意: 实现本接口时要关注acceptMonitor接口的返回值,如无特殊需求直接返回true，若返回false会拒绝本次连接。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Monitor {

    /**
     * 监控已接收到的连接
     *
     * @param channel 当前已经建立连接的通道对象
     * @return 非null:接受该连接,null:拒绝该连接
     */
    AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel);

    /**
     * 监控触发本次读回调Session的已读数据字节数
     *
     * @param session  当前执行read的Session对象
     * @param readSize 已读数据长度
     */
    void afterRead(Session session, int readSize);

    /**
     * 即将开始读取数据
     *
     * @param session 当前会话对象
     */
    void beforeRead(Session session);

    /**
     * 监控触发本次写回调session的已写数据字节数
     *
     * @param session   本次执行write回调的Session对象
     * @param writeSize 本次输出的数据长度
     */
    void afterWrite(Session session, int writeSize);

    /**
     * 即将开始写数据
     *
     * @param session 当前会话对象
     */
    void beforeWrite(Session session);

}
