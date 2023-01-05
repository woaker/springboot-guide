package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:32
 * @description Validate
 */
@Component
@Slf4j
public class YzValidateHandler extends TradeHandler {

    @Override
    public void doHandler(TradeHandlerContext context) {
        String reqId = "";
        context.setReqId(reqId);
        checkParams(context);
        next.doHandler(context);
    }

    private void checkParams(TradeHandlerContext context) {
        System.out.println("");
    }
}
