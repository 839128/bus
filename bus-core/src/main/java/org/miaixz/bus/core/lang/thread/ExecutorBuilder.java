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
package org.miaixz.bus.core.lang.thread;

import java.io.Serial;
import java.util.concurrent.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * {@link ThreadPoolExecutor} 建造者
 *
 * <pre>
 *     1. 如果池中任务数 &lt; corePoolSize     - 放入立即执行
 *     2. 如果池中任务数 &gt; corePoolSize     - 放入队列等待
 *     3. 队列满                              - 新建线程立即执行
 *     4. 执行中的线程 &gt; maxPoolSize        - 触发handler（RejectedExecutionHandler）异常
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExecutorBuilder implements Builder<ThreadPoolExecutor> {

    @Serial
    private static final long serialVersionUID = 2852279106156L;

    /**
     * 默认的等待队列容量
     */
    public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;
    /**
     * 初始池大小
     */
    private int corePoolSize;
    /**
     * 最大池大小（允许同时执行的最大线程数）
     */
    private int maxPoolSize = Integer.MAX_VALUE;
    /**
     * 线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长
     */
    private long keepAliveTime = TimeUnit.SECONDS.toNanos(60);
    /**
     * 队列，用于存放未执行的线程
     */
    private BlockingQueue<Runnable> workQueue;
    /**
     * 线程工厂，用于自定义线程创建
     */
    private ThreadFactory threadFactory;
    /**
     * 当线程阻塞（block）时的异常处理器，所谓线程阻塞即线程池和等待队列已满，无法处理线程时采取的策略
     */
    private RejectedExecutionHandler handler;
    /**
     * 线程执行超时后是否回收线程
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * 创建ExecutorBuilder，开始构建
     *
     * @return this
     */
    public static ExecutorBuilder of() {
        return new ExecutorBuilder();
    }

    /**
     * 构建ThreadPoolExecutor
     *
     * @param builder this
     * @return {@link ThreadPoolExecutor}
     */
    private static ThreadPoolExecutor build(final ExecutorBuilder builder) {
        final int corePoolSize = builder.corePoolSize;
        final int maxPoolSize = builder.maxPoolSize;
        final long keepAliveTime = builder.keepAliveTime;
        final BlockingQueue<Runnable> workQueue;
        if (null != builder.workQueue) {
            workQueue = builder.workQueue;
        } else {
            // corePoolSize为0则要使用SynchronousQueue避免无限阻塞
            workQueue = (corePoolSize <= 0) ? new SynchronousQueue<>()
                    : new LinkedBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        }
        final ThreadFactory threadFactory = (null != builder.threadFactory) ? builder.threadFactory
                : Executors.defaultThreadFactory();
        final RejectedExecutionHandler handler = ObjectKit.defaultIfNull(builder.handler,
                RejectPolicy.ABORT.getValue());

        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(//
                corePoolSize, //
                maxPoolSize, //
                keepAliveTime, TimeUnit.NANOSECONDS, //
                workQueue, //
                threadFactory, //
                handler//
        );
        if (null != builder.allowCoreThreadTimeOut) {
            threadPoolExecutor.allowCoreThreadTimeOut(builder.allowCoreThreadTimeOut);
        }
        return threadPoolExecutor;
    }

    /**
     * 设置初始池大小，默认0
     *
     * @param corePoolSize 初始池大小
     * @return this
     */
    public ExecutorBuilder setCorePoolSize(final int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    /**
     * 设置最大池大小（允许同时执行的最大线程数）
     *
     * @param maxPoolSize 最大池大小（允许同时执行的最大线程数）
     * @return this
     */
    public ExecutorBuilder setMaxPoolSize(final int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    /**
     * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长
     *
     * @param keepAliveTime 线程存活时间
     * @param unit          单位
     * @return this
     */
    public ExecutorBuilder setKeepAliveTime(final long keepAliveTime, final TimeUnit unit) {
        return setKeepAliveTime(unit.toNanos(keepAliveTime));
    }

    /**
     * 设置线程存活时间，即当池中线程多于初始大小时，多出的线程保留的时长，单位纳秒
     *
     * @param keepAliveTime 线程存活时间，单位纳秒
     * @return this
     */
    public ExecutorBuilder setKeepAliveTime(final long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    /**
     * 设置队列，用于存在未执行的线程 可选队列有：
     *
     * <pre>
     * 1. {@link SynchronousQueue}    它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     * 2. {@link LinkedBlockingQueue} 默认无界队列，当运行线程大于corePoolSize时始终放入此队列，此时maxPoolSize无效。
     *                        当构造LinkedBlockingQueue对象时传入参数，变为有界队列，队列满时，运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     * 3. {@link ArrayBlockingQueue}  有界队列，相对无界队列有利于控制队列大小，队列满时，运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     * </pre>
     *
     * @param workQueue 队列
     * @return this
     */
    public ExecutorBuilder setWorkQueue(final BlockingQueue<Runnable> workQueue) {
        this.workQueue = workQueue;
        return this;
    }

    /**
     * 使用{@link LinkedBlockingQueue} 作为等待队列 队列满时，运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     *
     * @param capacity 队列容量
     * @return this
     */
    public ExecutorBuilder useLinkedBlockingQueue(final int capacity) {
        return setWorkQueue(new LinkedBlockingQueue<>(capacity));
    }

    /**
     * 使用{@link ArrayBlockingQueue} 做为等待队列 有界队列，相对无界队列有利于控制队列大小，队列满时，运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     *
     * @param capacity 队列容量
     * @return this
     */
    public ExecutorBuilder useArrayBlockingQueue(final int capacity) {
        return setWorkQueue(new ArrayBlockingQueue<>(capacity));
    }

    /**
     * 使用{@link SynchronousQueue} 做为等待队列（非公平策略） 它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     *
     * @return this
     */
    public ExecutorBuilder useSynchronousQueue() {
        return useSynchronousQueue(false);
    }

    /**
     * 使用{@link SynchronousQueue} 做为等待队列 它将任务直接提交给线程而不保持它们。当运行线程小于maxPoolSize时会创建新线程，否则触发异常策略
     *
     * @param fair 是否使用公平访问策略
     * @return this
     */
    public ExecutorBuilder useSynchronousQueue(final boolean fair) {
        return setWorkQueue(new SynchronousQueue<>(fair));
    }

    /**
     * 设置线程工厂，用于自定义线程创建
     *
     * @param threadFactory 线程工厂
     * @return this
     * @see ThreadFactoryBuilder
     */
    public ExecutorBuilder setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * 设置当线程阻塞（block）时的异常处理器，所谓线程阻塞即线程池和等待队列已满，无法处理线程时采取的策略 此处可以使用JDK预定义的几种策略，见{@link RejectPolicy}枚举
     *
     * @param handler {@link RejectedExecutionHandler}
     * @return this
     * @see RejectPolicy
     */
    public ExecutorBuilder setHandler(final RejectedExecutionHandler handler) {
        this.handler = handler;
        return this;
    }

    /**
     * 设置线程执行超时后是否回收线程
     *
     * @param allowCoreThreadTimeOut 线程执行超时后是否回收线程
     * @return this
     */
    public ExecutorBuilder setAllowCoreThreadTimeOut(final boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    /**
     * 构建ThreadPoolExecutor
     */
    @Override
    public ThreadPoolExecutor build() {
        return build(this);
    }

    /**
     * 创建有回收关闭功能的ExecutorService
     *
     * @return 创建有回收关闭功能的ExecutorService
     */
    public ExecutorService buildFinalizable() {
        return new FinalizableDelegatedExecutorService(build());
    }

}
