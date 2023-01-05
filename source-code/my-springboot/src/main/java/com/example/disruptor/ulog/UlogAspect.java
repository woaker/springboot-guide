package com.example.disruptor.ulog;

import com.alibaba.fastjson.JSON;
import com.example.disruptor.ulog.annotation.Ulog;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UlogAspect implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UlogAspect.class);

    @Autowired
    private UserLogEventProducer userLogEventProducer;

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("UlogAspect start........");

    }

    public Object afterReturning(JoinPoint joinPoint, Object retValue) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Ulog ulog = method.getDeclaredAnnotation(Ulog.class);
            Object[] args = joinPoint.getArgs();
            int uid = 0;
            String uname = "";
            userLogEventProducer.publish(uid, uname, DateUtils.getCurrentDateTime2String(""), ulog.type(), ulog.bizId(), makeAfterString(args, retValue), getRequestUrl(), method.toString());
        } catch (Exception e) {
            LOGGER.error("system error ! afterReturning", UlogAspect.class, e);
        }
        return joinPoint;
    }

    private static String makeAfterString(Object[] args, Object retValue) {
        Map<String, String> afterStringMap = new HashMap<>();
        afterStringMap.put("parameter", JSON.toJSONString(args));
        afterStringMap.put("return", JSON.toJSONString(retValue));
        return JSON.toJSONString(afterStringMap);
    }

    private static String getRequestUrl() {
        String optPath = Strings.EMPTY;
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null
                    && ((ServletRequestAttributes) requestAttributes) != null
                    && ((ServletRequestAttributes) requestAttributes).getRequest() != null
                    && ((ServletRequestAttributes) requestAttributes).getRequest().getRequestURL() != null
            ) {
                optPath = ((ServletRequestAttributes) requestAttributes).getRequest().getRequestURL().toString();
            }
        } catch (Exception e) {
            log.warn("getRequestUrl:getRequestUrl:error!", e);
        }
        return optPath;
    }
}
