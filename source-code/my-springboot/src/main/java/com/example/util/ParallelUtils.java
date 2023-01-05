package com.example.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.function.FailableFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author yongjun.xiao
 */
public class ParallelUtils {

    private ParallelUtils() {

    }

    /**
     * 对给定的一组参数进行并行计算, 使用fast fail策略.
     *
     * @param executor    {@link ThreadPoolExecutor} 线程池
     * @param params      一组参数
     * @param transformer 计算逻辑
     * @param <T>         参数类型
     * @param <R>         计算结果类型
     * @return list of result
     * @throws RuntimeException 如果计算出错
     */
    public static <T, R> List<R> executeParallel(ThreadPoolExecutor executor, List<T> params,
                                                 FailableFunction<T, R, Exception> transformer) {
        if (CollUtil.isEmpty(params)) {
            return Collections.emptyList();
        }

        List<Future<R>> futures = StreamEx
                .of(params)
                .map(t -> executor.submit(() -> {
                    try {
                        return transformer.apply(t);
                    } catch (Exception e) {
                        throw ExceptionUtil.wrapRuntime(e);
                    }
                }))
                .toImmutableList();

        List<R> result = new ArrayList<>(params.size());

        for (Future<R> future : futures) {
            try {
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // fast fail
                throw ExceptionUtil.wrapRuntime(e);
            }
        }

        return Collections.unmodifiableList(result);
    }

    public static <T, R> List<R> executePartitionsParallel(ThreadPoolExecutor executor, List<List<T>> params,
                                                           FailableFunction<List<T>, List<R>, Exception> transformer) {
        if (CollUtil.isEmpty(params)) {
            return Collections.emptyList();
        }

        List<Future<List<R>>> futures = StreamEx
                .of(params)
                .map(t -> executor.submit(() -> {
                    try {
                        return transformer.apply(t);
                    } catch (Exception e) {
                        throw ExceptionUtil.wrapRuntime(e);
                    }
                }))
                .toImmutableList();

        List<List<R>> result = new ArrayList<>(params.size());

        for (Future<List<R>> future : futures) {
            try {
                result.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                // fast fail
                throw ExceptionUtil.wrapRuntime(e);
            }
        }

        return result.stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

}
