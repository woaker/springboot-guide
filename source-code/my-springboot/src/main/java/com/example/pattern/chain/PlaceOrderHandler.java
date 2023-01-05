package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:40
 * @description PlaceOrder
 */
@Component
@Slf4j
public class PlaceOrderHandler extends TradeHandler {

    @Override
    public void doHandler(TradeHandlerContext context) {
      /*  Map<String, String> map = new HashMap<>(2);
        map.put("ip", context.getIpAddr());
        map.put("userAgent", context.getUserAgent());
        OrderResult result = tradeOrderAndPayService.placeOrder(context.getParam(), context.getReqId(), JSON.toJSONString(map));
        context.setOrderResult(result);*/
        next.doHandler(context);
    }
}
