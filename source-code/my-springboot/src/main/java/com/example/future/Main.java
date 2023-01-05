package com.example.future;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author yongjun.xiao
 * @Description TODO
 * @createTime 2023年01月05日
 */
@Slf4j
public class Main {

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    public void main(String[] args) {
        List<Long> userIds = new ArrayList<>();
        Map<Long, List<BabyInfoResp>> babyInfoMap = getBabyInfoByUserIds(userIds);
    }

    private Map<Long, List<BabyInfoResp>> getBabyInfoByUserIds(List<Long> userIds) {

        CountDownLatch latch = new CountDownLatch(userIds.size());

        Map<Long, List<BabyInfoResp>> result = new HashMap<>();

        Map<Long, Future<List<BabyInfoResp>>> taskMap = new HashMap<>();
        userIds.forEach(userId -> {
            Future<List<BabyInfoResp>> futureTask = taskExecutor.submit(new GetDataSourceTask(userId, latch));
            taskMap.put(userId, futureTask);
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("多线程执行异常1：", e);
        }

        try {
            for (Map.Entry<Long, Future<List<BabyInfoResp>>> task : taskMap.entrySet()) {
                result.put(task.getKey(), task.getValue().get());
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("多线程执行异常2：", e);
        }

        return result;
    }


    public class GetDataSourceTask implements Callable<List<BabyInfoResp>> {

        private final Long userId;
        private final CountDownLatch latch;

        public GetDataSourceTask(Long userId, CountDownLatch latch) {
            this.userId = userId;
            this.latch = latch;
        }

        @Override
        public List<BabyInfoResp> call() {

            try {
                return Collections.EMPTY_LIST;// rpcProxy.getBabyInfoByUserId(userId);
            } catch (Exception e) {
                log.error("调用rpc[queryBabyInfoListByParentId]异常, req:{}, error:{}.", userId, e.getMessage());
            } finally {
                latch.countDown();
            }

            return Collections.EMPTY_LIST;
        }
    }
}
