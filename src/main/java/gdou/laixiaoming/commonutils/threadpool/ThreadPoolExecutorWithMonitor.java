package gdou.laixiaoming.commonutils.threadpool;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.*;

public class ThreadPoolExecutorWithMonitor
        implements AutoCloseable, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorWithMonitor.class);

    //默认值
    private static final int DEFAULT_QUEUE_SIZE = 1000;
    private static final int DEFAULT_POOL_SIZE = 10;
    private static final int DEFAULT_MAX_POOL_SIZE = 200;
    private static final long DEFAULT_REFRESH_PERIOD = 10;
    private static final String DEFAULT_THREAD_POOL_NAME = "async thread pool executor";

    private int queueSize = DEFAULT_QUEUE_SIZE;
    private int poolSize = DEFAULT_POOL_SIZE;
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
    private String threadPoolName = DEFAULT_THREAD_POOL_NAME;
    private long refreshPeriodMinutes = DEFAULT_REFRESH_PERIOD;

    /**
     * 用于周期性监控线程池的运行状态
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 自定义异步线程池
     *(1)任务队列使用有界队列
     *(2)自定义拒绝策略
     */
    private ThreadPoolExecutor threadPoolExecutor;

    private void initThreadPool() {
        threadPoolExecutor = new ThreadPoolExecutor(
                poolSize,
                maxPoolSize,
                0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue(queueSize),
                new BasicThreadFactory.Builder().namingPattern("async-thread-%d").build(),
                (r, executor) -> logger.error("{} is full! ! ", threadPoolName));
    }

    private void initMonitor() {
        scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor(
                        new BasicThreadFactory.Builder().namingPattern(threadPoolName + " monitor").build());
        scheduledExecutorService.scheduleAtFixedRate(()-> {

            /**
             * 线程池需要执行的任务数
             */
            long taskCount = threadPoolExecutor.getTaskCount();
            /**
             * 线程池在运行过程中已完成的任务数
             */
            long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
            /**
             * 曾经创建过的最大线程数
             */
            long largestPoolSize = threadPoolExecutor.getLargestPoolSize();
            /**
             * 线程池里的线程数量
             */
            long poolSize = threadPoolExecutor.getPoolSize();
            /**
             * 线程池里活跃的线程数量
             */
            long activeCount = threadPoolExecutor.getActiveCount();

            logger.info("{} monitor>>>>\ntaskCount:{}, completedTaskCount:{}, largestPoolSize:{}, poolSize:{}, activeCount:{} \n<<<<",
                    threadPoolName, taskCount, completedTaskCount, largestPoolSize, poolSize,activeCount);
        }, 0, refreshPeriodMinutes, TimeUnit.SECONDS);
    }

    public void execute(Runnable task) {
        threadPoolExecutor.execute(task);
    }

    @Override
    public void close() throws Exception {
        threadPoolExecutor.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initThreadPool();
        initMonitor();
    }

    public static ThreadPoolExecutorWithMonitor.ThreadPoolExecutorWithMonitorBuilder builder() {
        return new ThreadPoolExecutorWithMonitor.ThreadPoolExecutorWithMonitorBuilder();
    }

    public static final class ThreadPoolExecutorWithMonitorBuilder {
        private int queueSize = DEFAULT_QUEUE_SIZE;
        private int poolSize = DEFAULT_POOL_SIZE;
        private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        private String threadPoolName = DEFAULT_THREAD_POOL_NAME;
        private long refreshPeriodMinutes = DEFAULT_REFRESH_PERIOD;

        private ThreadPoolExecutorWithMonitorBuilder() {
        }

        public static ThreadPoolExecutorWithMonitorBuilder aThreadPoolExecutorWithMonitor() {
            return new ThreadPoolExecutorWithMonitorBuilder();
        }

        public ThreadPoolExecutorWithMonitorBuilder queueSize(int queueSize) {
            this.queueSize = queueSize;
            return this;
        }

        public ThreadPoolExecutorWithMonitorBuilder poolSize(int poolSize) {
            this.poolSize = poolSize;
            return this;
        }

        public ThreadPoolExecutorWithMonitorBuilder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        public ThreadPoolExecutorWithMonitorBuilder threadPoolName(String threadPoolName) {
            this.threadPoolName = threadPoolName;
            return this;
        }

        public ThreadPoolExecutorWithMonitorBuilder refreshPeriodMinutes(long refreshPeriodMinutes) {
            this.refreshPeriodMinutes = refreshPeriodMinutes;
            return this;
        }

        public ThreadPoolExecutorWithMonitor build() {
            ThreadPoolExecutorWithMonitor threadPoolExecutorWithMonitor = new ThreadPoolExecutorWithMonitor();
            threadPoolExecutorWithMonitor.queueSize = this.queueSize;
            threadPoolExecutorWithMonitor.poolSize = this.poolSize;
            threadPoolExecutorWithMonitor.maxPoolSize = this.maxPoolSize;
            threadPoolExecutorWithMonitor.threadPoolName = this.threadPoolName;
            threadPoolExecutorWithMonitor.refreshPeriodMinutes = this.refreshPeriodMinutes;
            return threadPoolExecutorWithMonitor;
        }
    }
}