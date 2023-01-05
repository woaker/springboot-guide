package com.example.future;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author eli.yang
 * @date 2022/8/10 18:11
 * @description
 */
@Configuration
public class TaskExecutorConfiguration {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolExecutorMdcUtil threadPoolTaskExecutor = new ThreadPoolExecutorMdcUtil();
        threadPoolTaskExecutor.setThreadNamePrefix("common-task-executor-");
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(8);
        threadPoolTaskExecutor.setQueueCapacity(2000);
        threadPoolTaskExecutor.setKeepAliveSeconds(300);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return threadPoolTaskExecutor;
    }

    /**
     * @author eli.yang
     * @date 2022/8/10 18:12
     * @description 将MDC写入多线程的线程池工具，实例化的时候需要无参构造然后set
     */
    static class ThreadPoolExecutorMdcUtil extends ThreadPoolTaskExecutor {

        private static final long serialVersionUID = 1L;

        // 重写执行方法，将MDC塞入
        @Override
        public void execute(Runnable task) {
            super.execute(TraceIdUtils.wrap(task, MDC.getCopyOfContextMap()));
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return super.submit(TraceIdUtils.wrap(task, MDC.getCopyOfContextMap()));
        }

        @Override
        public Future<?> submit(Runnable task) {
            return super.submit(TraceIdUtils.wrap(task, MDC.getCopyOfContextMap()));
        }
    }
}
