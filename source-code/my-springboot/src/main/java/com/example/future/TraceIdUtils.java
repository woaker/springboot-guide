package com.example.future;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author eli.yang
 * @date 2022/8/10 18:12
 * @description
 */
@Slf4j
public class TraceIdUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceIdUtils.class);

    public static void setTraceAndSpanId() {

        try {
            if (StrUtil.isNotEmpty(getTraceId())) {
                return;
            }

            String traceIdThreadLocal = "TraceGenerater2.getTracer().getTraceIdThreadLocal();";
            String spanIdThreadLocal = "TraceGenerater2.getTracer().getSpanIdThreadLocal()";
            if (StrUtil.isAllBlank(traceIdThreadLocal, spanIdThreadLocal)) {
                MDC.put("TraceId", UUID.randomUUID().toString().replace("-", ""));
                MDC.put("SpanId", "");
                return;
            }

            String traceID = new String(traceIdThreadLocal.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String spanID = new String(spanIdThreadLocal.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            MDC.put("TraceId", traceID);
            MDC.put("SpanId", spanID);
        } catch (Exception e) {
            LOGGER.info("XDCSTracer|error:{}", e.getMessage(), e);
        }
    }

    /**
     * @param <T>
     * @param callable
     * @param context
     * @return
     */
    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceAndSpanId();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceAndSpanId();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }

    /**
     * 获取traceID
     *
     * @return
     */
    public static String getTraceId() {

        try {
            return MDC.get("TraceId");
        } catch (Exception e) {
            log.warn("获取traceId异常:", e);
        }

        return "";
    }

}
