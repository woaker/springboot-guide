package com.example.disruptor.ulog.annotation;

import com.example.disruptor.ulog.BizId;
import com.example.disruptor.ulog.UlogType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Ulog {
	
	/**
     * 调用服务名
     */
    UlogType type();
    /**
     * 调用的方式
     */
    BizId bizId();

}
