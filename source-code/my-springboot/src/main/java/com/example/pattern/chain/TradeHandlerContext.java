package com.example.pattern.chain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:42
 * @description context
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeHandlerContext {

    private long handlerStartTime;

    private String reqId;

    private String ipAddr;

    private HttpServletRequest servletRequest;

    private String userAgent;

    private String app;
}
