package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: johnvon.feng@ximalaya.com
 * @date: 2022-05-27 23:00
 **/
@Slf4j
@Component
public class OuterValidateHandler extends TradeHandler {

    @Override
    public void doHandler(TradeHandlerContext context) {
        String reqId = "channelRequestIdGenerateService.generateChannelRequestId();";
        context.setReqId(reqId);
     //   checkParams(context);
        next.doHandler(context);
    }
}
