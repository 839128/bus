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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.thread.*;

/**
 * 线程池工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadKit {

    /**
     * 获得一个新的线程池，默认的策略如下：
     * 
     * <pre>
     *    1. 初始线程数为 0
     *    2. 最大线程数为Integer.MAX_VALUE
     *    3. 使用SynchronousQueue
     *    4. 任务直接提交给线程而不保持它们
     * </pre>
     *
     * @return {@link ExecutorService}
     */
    public static ExecutorService newExecutor() {
        return ExecutorBuilder.of().useSynchronousQueue().build();
    }

    /**
     * 获得一个新的线程池，只有单个线程，策略如下：
     * 
     * <pre>
     *    1. 初始线程数为 1
     *    2. 最大线程数为 1
     *    3. 默认使用LinkedBlockingQueue，默认队列大小为1024
     *    4. 同时只允许一个线程工作，剩余放入队列等待，等待数超过1024报错
     * </pre>
     *
     * @return {@link ExecutorService}
     */
    public static ExecutorService newSingleExecutor() {
        return ExecutorBuilder.of().setCorePoolSize(1).setMaxPoolSize(1).setKeepAliveTime(0).buildFinalizable();
    }

    /**
     * 新建一个线程池，默认的策略如下：
     * 
     * <pre>
     *    1. 初始线程数为poolSize指定的大小
     *    2. 最大线程数为poolSize指定的大小
     *    3. 默认使用LinkedBlockingQueue，默认无界队列
     * </pre>
     *
     * @param poolSize 同时执行的线程数大小
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutor(final int poolSize) {
        return newExecutor(poolSize, poolSize);
    }

    /**
     * 获得一个新的线程池 如果maximumPoolSize &gt;= corePoolSize，在没有新任务加入的情况下，多出的线程将最多保留60s
     *
     * @param corePoolSize    初始线程池大小
     * @param maximumPoolSize 最大线程池大小
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutor(final int corePoolSize, final int maximumPoolSize) {
        return ExecutorBuilder.of().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).build();
    }

    /**
     * 获得一个新的线程池，并指定最大任务队列大小 如果maximumPoolSize &gt;= corePoolSize，在没有新任务加入的情况下，多出的线程将最多保留60s
     *
     * @param corePoolSize     初始线程池大小
     * @param maximumPoolSize  最大线程池大小
     * @param maximumQueueSize 最大任务队列大小
     * @return {@link ThreadPoolExecutor}
     */
    public static ExecutorService newExecutor(final int corePoolSize, final int maximumPoolSize,
            final int maximumQueueSize) {
        return ExecutorBuilder.of().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize)
                .useLinkedBlockingQueue(maximumQueueSize).build();
    }

    /**
     * 获得一个新的线程池 传入阻塞系数，线程池的大小计算公式为：CPU可用核心数 / (1 - 阻塞因子) Blocking Coefficient(阻塞系数) = 阻塞时间／（阻塞时间+使用CPU的时间）
     * 计算密集型任务的阻塞系数为0，而IO密集型任务的阻塞系数则接近于1。
     *
     * <p>
     * see: <a href=
     * "http://blog.csdn.net/partner4java/article/details/9417663">http://blog.csdn.net/partner4java/article/details/9417663</a>
     * </p>
     *
     * @param blockingCoefficient 阻塞系数，阻塞因子介于0~1之间的数，阻塞因子越大，线程池中的线程数越多。
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(final float blockingCoefficient) {
        if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
            throw new IllegalArgumentException("[blockingCoefficient] must between 0 and 1, or equals 0.");
        }

        // 最佳的线程数 = CPU可用核心数 / (1 - 阻塞系数)
        final int poolSize = (int) (RuntimeKit.getProcessorCount() / (1 - blockingCoefficient));
        return ExecutorBuilder.of().setCorePoolSize(poolSize).setMaxPoolSize(poolSize).setKeepAliveTime(0L).build();
    }

    /**
     * 获取一个新的线程池，默认的策略如下
     * 
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用LinkedBlockingQueue，默认队列大小为1024
     *     3. 如果isBlocked为{@code
     * true
     * }，当执行拒绝策略的时候会处于阻塞状态，直到能添加到队列中或者被{@link Thread#interrupt()}中断
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param threadNamePrefix 线程名称前缀
     * @param isBlocked        是否使用{@link BlockPolicy}策略
     * @return {@link ExecutorService}
     */
    public static ExecutorService newFixedExecutor(final int nThreads, final String threadNamePrefix,
            final boolean isBlocked) {
        return newFixedExecutor(nThreads, 1024, threadNamePrefix, isBlocked);
    }

    /**
     * 获取一个新的线程池，默认的策略如下
     * 
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用LinkedBlockingQueue
     *     3. 如果isBlocked为{@code
     * true
     * }，当执行拒绝策略的时候会处于阻塞状态，直到能添加到队列中或者被{@link Thread#interrupt()}中断
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param maximumQueueSize 队列大小
     * @param threadNamePrefix 线程名称前缀
     * @param isBlocked        是否使用{@link BlockPolicy}策略
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newFixedExecutor(final int nThreads, final int maximumQueueSize,
            final String threadNamePrefix, final boolean isBlocked) {
        return newFixedExecutor(nThreads, maximumQueueSize, threadNamePrefix,
                (isBlocked ? RejectPolicy.BLOCK : RejectPolicy.ABORT).getValue());
    }

    /**
     * 获得一个新的线程池，默认策略如下
     * 
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用LinkedBlockingQueue
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param maximumQueueSize 队列大小
     * @param threadNamePrefix 线程名称前缀
     * @param handler          拒绝策略
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newFixedExecutor(final int nThreads, final int maximumQueueSize,
            final String threadNamePrefix, final RejectedExecutionHandler handler) {
        return ExecutorBuilder.of().setCorePoolSize(nThreads).setMaxPoolSize(nThreads)
                .setWorkQueue(new LinkedBlockingQueue<>(maximumQueueSize))
                .setThreadFactory(createThreadFactory(threadNamePrefix)).setHandler(handler).build();
    }

    /**
     * 直接在公共线程池中执行线程
     *
     * @param runnable 可运行对象
     */
    public static void execute(final Runnable runnable) {
        GlobalThreadPool.execute(runnable);
    }

    /**
     * 执行异步方法
     *
     * @param runnable 需要执行的方法体
     * @param isDaemon 是否守护线程。守护线程会在主线程结束后自动结束
     * @return 执行的方法体
     */
    public static Runnable execAsync(final Runnable runnable, final boolean isDaemon) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(isDaemon);
        thread.start();

        return runnable;
    }

    /**
     * 执行有返回值的异步方法 Future代表一个异步执行的操作，通过get()方法可以获得操作的结果，如果异步操作还没有完成，则，get()会使当前线程阻塞
     *
     * @param <T>  回调对象类型
     * @param task {@link Callable}
     * @return {@link Future}
     */
    public static <T> Future<T> execAsync(final Callable<T> task) {
        return GlobalThreadPool.submit(task);
    }

    /**
     * 执行有返回值的异步方法 Future代表一个异步执行的操作，通过get()方法可以获得操作的结果，如果异步操作还没有完成，则，get()会使当前线程阻塞
     *
     * @param runnable 可运行对象
     * @return {@link Future}
     */
    public static Future<?> execAsync(final Runnable runnable) {
        return GlobalThreadPool.submit(runnable);
    }

    /**
     * 新建一个CompletionService，调用其submit方法可以异步执行多个任务，最后调用take方法按照完成的顺序获得其结果。 若未完成，则会阻塞
     *
     * @param <T> 回调对象类型
     * @return {@link CompletionService}
     */
    public static <T> CompletionService<T> newCompletionService() {
        return new ExecutorCompletionService<>(GlobalThreadPool.getExecutor());
    }

    /**
     * 新建一个CompletionService，调用其submit方法可以异步执行多个任务，最后调用take方法按照完成的顺序获得其结果。 若未完成，则会阻塞
     *
     * @param <T>      回调对象类型
     * @param executor 执行器 {@link ExecutorService}
     * @return {@link CompletionService}
     */
    public static <T> CompletionService<T> newCompletionService(final ExecutorService executor) {
        return new ExecutorCompletionService<>(executor);
    }

    /**
     * 新建一个CountDownLatch，一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。 示例：等6个同学都离开教室，班长才能锁门。
     * 
     * <pre>{@code
     * CountDownLatch latch = new CountDownLatch(6); // 总共任务是6
     * for (int x = 0; x < 6; x++) {
     *     // 具体生产任务，可以用线程池替换
     *     new Thread(() -> {
     *         try {
     *             // 每个同学在教室待上几秒秒钟
     *             int time = ThreadLocalRandom.current().nextInt(1, 5);
     *             TimeUnit.SECONDS.sleep(time);
     *         } catch (InterruptedException e) {
     *             e.printStackTrace();
     *         }
     *         System.out.printf("同学【%s】，已经离开了教室%n", Thread.currentThread().getName());
     *         latch.countDown(); // 减1（每离开一个同学，减去1）,必须执行，可以放到 try...finally中
     *     }, "同学 - " + x).start();
     * }
     * latch.await(); // 等待计数为0后再解除阻塞；（等待所有同学离开）
     * System.out.println("【主线程】所有同学都离开了教室，开始锁教室大门了。");
     * }
     * </pre>
     * 
     * 该示例，也可以用：{@link Phaser} 移相器 进行实现
     *
     * @param taskCount 任务数量
     * @return {@link CountDownLatch}
     */
    public static CountDownLatch newCountDownLatch(final int taskCount) {
        return new CountDownLatch(taskCount);
    }

    /**
     * 新建一个CycleBarrier 循环栅栏，一个同步辅助类 示例：7个同学，集齐7个龙珠，7个同学一起召唤神龙；前后集齐了2次
     * 
     * <pre>{@code
     * AtomicInteger times = new AtomicInteger();
     * CyclicBarrier barrier = new CyclicBarrier(7, () -> {
     *     System.out.println("");
     *     System.out.println("");
     *     System.out.println("【循环栅栏业务处理】7个子线程 都收集了一颗龙珠，七颗龙珠已经收集齐全，开始召唤神龙。"
     *             + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
     *     times.getAndIncrement();
     * }); // 现在设置的栅栏的数量为2
     * for (int x = 0; x < 7; x++) { // 创建7个线程, 当然也可以使用线程池替换。
     *     new Thread(() -> {
     *         while (times.get() < 2) {
     *             try {
     *                 System.out.printf("【Barrier - 收集龙珠】当前的线程名称：%s%n", Thread.currentThread().getName());
     *                 int time = ThreadLocalRandom.current().nextInt(1, 10); // 等待一段时间，模拟线程的执行时间
     *                 TimeUnit.SECONDS.sleep(time); // 模拟业务延迟，收集龙珠的时间
     *                 barrier.await(); // 等待，凑够了7个等待的线程
     *                 System.err.printf("〖Barrier - 举起龙珠召唤神龙〗当前的线程名称：%s\t%s%n", Thread.currentThread().getName(),
     *                         LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
     *                 if (barrier.getParties() >= 7) {
     *                     barrier.reset(); // 重置栅栏，等待下一次的召唤。
     *                 }
     *             } catch (Exception e) {
     *                 e.printStackTrace();
     *             }
     *         }
     *     }, "线程 - " + x).start();
     * }
     * }</pre>
     * 
     * 该示例，也可以用：{@link Phaser} 移相器 进行实现
     *
     * @param taskCount 任务数量
     * @return {@link CyclicBarrier}
     */
    public static CyclicBarrier newCyclicBarrier(final int taskCount) {
        return new CyclicBarrier(taskCount);
    }

    /**
     * 新建一个Phaser，一个同步辅助类，jdk1.7提供，可以完全替代CountDownLatch； Pharser: 移相器、相位器，可重用同步屏障；
     * 功能可以替换：{@link CyclicBarrier}（固定线程）循环栅栏、{@link CountDownLatch}（固定计数）倒数计数、加上分层功能 示例1：等6个同学都离开教室，班长才能锁门。
     * 
     * <pre>{@code
     * Phaser phaser = new Phaser(6); // 总共任务是6
     * for (int x = 0; x < 6; x++) {
     *     // 具体生产任务，可以用线程池替换
     *     new Thread(() -> {
     *         try {
     *             // 每个同学在教室待上几秒秒钟
     *             int time = ThreadLocalRandom.current().nextInt(1, 5);
     *             TimeUnit.SECONDS.sleep(5);
     *         } catch (InterruptedException e) {
     *             e.printStackTrace();
     *         }
     *         System.out.printf("同学【%s】，已经离开了教室%n", Thread.currentThread().getName());
     *         phaser.arrive(); // 减1 等价于countDown()方法（每离开一个同学，减去1）,必须执行，可以放到 try...finally中
     *     }, "同学 - " + x).start();
     * }
     * phaser.awaitAdvance(phaser.getPhase()); // 等价于latch.await()方法 等待计数为0后再解除阻塞；（等待所有同学离开）
     * System.out.println("【主线程】所有同学都离开了教室，开始锁教室大门了。");
     * }
     * </pre>
     * 
     * 示例2：7个同学，集齐7个龙珠，7个同学一起召唤神龙； 只需要：phaser.arrive(); --&gt; phaser.arriveAndAwaitAdvance() //等待其他的线程就位
     * 该示例，也可以用：{@link CyclicBarrier} 进行实现
     *
     * @param taskCount 任务数量
     * @return {@link Phaser}
     */
    public static Phaser newPhaser(final int taskCount) {
        return new Phaser(taskCount);
    }

    /**
     * 创建新线程，非守护线程，正常优先级，线程组与当前线程的线程组一致
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @return {@link Thread}
     */
    public static Thread newThread(final Runnable runnable, final String name) {
        final Thread t = newThread(runnable, name, false);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    /**
     * 创建新线程
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @param isDaemon 是否守护线程
     * @return {@link Thread}
     */
    public static Thread newThread(final Runnable runnable, final String name, final boolean isDaemon) {
        final Thread t = new Thread(null, runnable, name);
        t.setDaemon(isDaemon);
        return t;
    }

    /**
     * 挂起当前线程
     *
     * @param timeout  挂起的时长
     * @param timeUnit 时长单位
     * @return 被中断返回false，否则true
     */
    public static boolean sleep(final Number timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
        } catch (final InterruptedException e) {
            // 重新标记线程为中断状态（恢复中断信息），让后续代码能感知到“线程曾被中断过”
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    /**
     * 挂起当前线程
     *
     * @param millis 挂起的毫秒数
     * @return 被中断返回false，否则true
     */
    public static boolean sleep(final Number millis) {
        if (millis == null) {
            return true;
        }
        return sleep(millis.longValue());
    }

    /**
     * 挂起当前线程
     *
     * @param millis 挂起的毫秒数
     * @return 被中断返回false，否则true
     */
    public static boolean sleep(final long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    /**
     * 考虑{@link Thread#sleep(long)}方法有可能时间不足给定毫秒数，此方法保证sleep时间不小于给定的毫秒数
     *
     * @param millis 给定的sleep时间
     * @return 被中断返回false，否则true
     * @see ThreadKit#sleep(Number)
     */
    public static boolean safeSleep(final Number millis) {
        if (millis == null) {
            return true;
        }

        return safeSleep(millis.longValue());
    }

    /**
     * 考虑{@link Thread#sleep(long)}方法有可能时间不足给定毫秒数，此方法保证sleep时间不小于给定的毫秒数
     *
     * @param millis 给定的sleep时间（毫秒）
     * @return 被中断返回false，否则true
     * @see ThreadKit#sleep(Number)
     */
    public static boolean safeSleep(final long millis) {
        long done = 0;
        long before;
        // done表示实际花费的时间，确保实际花费时间大于应该sleep的时间
        while (done < millis) {
            before = System.nanoTime();
            if (!sleep(millis - done)) {
                return false;
            }
            // done始终为正
            done += (System.nanoTime() - before) / 1_000_000;
        }
        return true;
    }

    /**
     * @return 获得堆栈列表
     */
    public static StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * 获得堆栈项
     *
     * @param i 第几个堆栈项
     * @return 堆栈项
     */
    public static StackTraceElement getStackTraceElement(int i) {
        final StackTraceElement[] stackTrace = getStackTrace();
        if (i < 0) {
            i += stackTrace.length;
        }
        return stackTrace[i];
    }

    /**
     * 创建本地线程对象
     *
     * @param <T>           持有对象类型
     * @param isInheritable 是否为子线程提供从父线程那里继承的值
     * @return 本地线程
     */
    public static <T> ThreadLocal<T> createThreadLocal(final boolean isInheritable) {
        if (isInheritable) {
            return new InheritableThreadLocal<>();
        } else {
            return new ThreadLocal<>();
        }
    }

    /**
     * 创建本地线程对象
     *
     * @param <T>      持有对象类型
     * @param supplier 初始化线程对象函数
     * @return 本地线程
     * @see ThreadLocal#withInitial(Supplier)
     */
    public static <T> ThreadLocal<T> createThreadLocal(final Supplier<? extends T> supplier) {
        return ThreadLocal.withInitial(supplier);
    }

    /**
     * 创建ThreadFactoryBuilder
     *
     * @return ThreadFactoryBuilder
     * @see ThreadFactoryBuilder#build()
     */
    public static ThreadFactoryBuilder createThreadFactoryBuilder() {
        return ThreadFactoryBuilder.of();
    }

    /**
     * 创建自定义线程名称前缀的{@link ThreadFactory}
     *
     * @param threadNamePrefix 线程名称前缀
     * @return {@link ThreadFactory}
     * @see ThreadFactoryBuilder#build()
     */
    public static ThreadFactory createThreadFactory(final String threadNamePrefix) {
        return ThreadFactoryBuilder.of().setNamePrefix(threadNamePrefix).build();
    }

    /**
     * 结束线程，调用此方法后，线程将抛出 {@link InterruptedException}异常
     *
     * @param thread 线程
     * @param isJoin 是否等待结束
     */
    public static void interrupt(final Thread thread, final boolean isJoin) {
        if (null != thread && !thread.isInterrupted()) {
            thread.interrupt();
            if (isJoin) {
                waitForDie(thread);
            }
        }
    }

    /**
     * 等待当前线程结束. 调用 {@link Thread#join()} 并忽略 {@link InterruptedException}
     */
    public static void waitForDie() {
        waitForDie(Thread.currentThread());
    }

    /**
     * 等待线程结束. 调用 {@link Thread#join()} 并忽略 {@link InterruptedException}
     *
     * @param thread 线程
     */
    public static void waitForDie(final Thread thread) {
        if (null == thread) {
            return;
        }

        boolean dead = false;
        do {
            try {
                thread.join();
                dead = true;
            } catch (final InterruptedException e) {
                // ignore
            }
        } while (!dead);
    }

    /**
     * 获取JVM中与当前线程同组的所有线程
     *
     * @return 线程对象数组
     */
    public static Thread[] getThreads() {
        return getThreads(Thread.currentThread().getThreadGroup().getParent());
    }

    /**
     * 获取JVM中与当前线程同组的所有线程 使用数组二次拷贝方式，防止在线程列表获取过程中线程终止
     *
     * @param group 线程组
     * @return 线程对象数组
     */
    public static Thread[] getThreads(final ThreadGroup group) {
        final Thread[] slackList = new Thread[group.activeCount() * 2];
        final int actualSize = group.enumerate(slackList);
        final Thread[] result = new Thread[actualSize];
        System.arraycopy(slackList, 0, result, 0, actualSize);
        return result;
    }

    /**
     * 获取进程的主线程
     *
     * @return 进程的主线程
     */
    public static Thread getMainThread() {
        for (final Thread thread : getThreads()) {
            if (thread.getId() == 1) {
                return thread;
            }
        }
        return null;
    }

    /**
     * 获取当前线程的线程组
     *
     * @return 线程组
     */
    public static ThreadGroup currentThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    /**
     * 获取当前线程ID，即TID
     *
     * @return TID
     */
    public static long currentThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * 创建线程工厂
     *
     * @param prefix   线程名前缀
     * @param isDaemon 是否守护线程
     * @return {@link ThreadFactory}
     */
    public static ThreadFactory newNamedThreadFactory(final String prefix, final boolean isDaemon) {
        return new NamedThreadFactory(prefix, isDaemon);
    }

    /**
     * 创建线程工厂
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组，可以为null
     * @param isDaemon    是否守护线程
     * @return {@link ThreadFactory}
     */
    public static ThreadFactory newNamedThreadFactory(final String prefix, final ThreadGroup threadGroup,
            final boolean isDaemon) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon);
    }

    /**
     * 创建线程工厂
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组，可以为null
     * @param isDaemon    是否守护线程
     * @param handler     未捕获异常处理
     * @return {@link ThreadFactory}
     */
    public static ThreadFactory newNamedThreadFactory(final String prefix, final ThreadGroup threadGroup,
            final boolean isDaemon, final UncaughtExceptionHandler handler) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon, handler);
    }

    /**
     * 阻塞当前线程，保证在main方法中执行不被退出
     *
     * @param object 对象所在线程
     */
    public static void sync(final Object object) {
        synchronized (object) {
            try {
                object.wait();
            } catch (final InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * 并发测试 此方法用于测试多线程下执行某些逻辑的并发性能 调用此方法会导致当前线程阻塞。 结束后可调用{@link ConcurrencyTester#getInterval()} 方法获取执行时间
     *
     * @param threadSize 并发线程数
     * @param runnable   执行的逻辑实现
     * @return {@link ConcurrencyTester}
     */
    public static ConcurrencyTester concurrencyTest(final int threadSize, final Runnable runnable) {
        try (ConcurrencyTester tester = new ConcurrencyTester(threadSize)) {
            return tester.test(runnable);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link ScheduledThreadPoolExecutor}
     *
     * @param corePoolSize 初始线程池大小
     * @return {@link ScheduledThreadPoolExecutor}
     */
    public static ScheduledThreadPoolExecutor createScheduledExecutor(final int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * 开始执行一个定时任务，执行方式分fixedRate模式和fixedDelay模式。 注意：此方法的延迟和周期的单位均为毫秒。
     *
     * <ul>
     * <li>fixedRate 模式：以固定的频率执行。每period的时刻检查，如果上个任务完成，启动下个任务，否则等待上个任务结束后立即启动。</li>
     * <li>fixedDelay模式：以固定的延时执行。上次任务结束后等待period再执行下个任务。</li>
     * </ul>
     *
     * @param executor              定时任务线程池，{@code null}新建一个默认线程池
     * @param command               需要定时执行的逻辑
     * @param initialDelay          初始延迟，单位毫秒
     * @param period                执行周期，单位毫秒
     * @param fixedRateOrFixedDelay {@code true}表示fixedRate模式，{@code false}表示fixedDelay模式
     * @return {@link ScheduledExecutorService}
     */
    public static ScheduledExecutorService schedule(final ScheduledExecutorService executor, final Runnable command,
            final long initialDelay, final long period, final boolean fixedRateOrFixedDelay) {
        return schedule(executor, command, initialDelay, period, TimeUnit.MILLISECONDS, fixedRateOrFixedDelay);
    }

    /**
     * 开始执行一个定时任务，执行方式分fixedRate模式和fixedDelay模式。
     *
     * <ul>
     * <li>fixedRate 模式：以固定的频率执行。每period的时刻检查，如果上个任务完成，启动下个任务，否则等待上个任务结束后立即启动。</li>
     * <li>fixedDelay模式：以固定的延时执行。上次任务结束后等待period再执行下个任务。</li>
     * </ul>
     *
     * @param executor              定时任务线程池，{@code null}新建一个默认线程池
     * @param command               需要定时执行的逻辑
     * @param initialDelay          初始延迟
     * @param period                执行周期
     * @param timeUnit              时间单位
     * @param fixedRateOrFixedDelay {@code true}表示fixedRate模式，{@code false}表示fixedDelay模式
     * @return {@link ScheduledExecutorService}
     */
    public static ScheduledExecutorService schedule(ScheduledExecutorService executor, final Runnable command,
            final long initialDelay, final long period, final TimeUnit timeUnit, final boolean fixedRateOrFixedDelay) {
        if (null == executor) {
            executor = createScheduledExecutor(2);
        }
        if (fixedRateOrFixedDelay) {
            executor.scheduleAtFixedRate(command, initialDelay, period, timeUnit);
        } else {
            executor.scheduleWithFixedDelay(command, initialDelay, period, timeUnit);
        }

        return executor;
    }

}
