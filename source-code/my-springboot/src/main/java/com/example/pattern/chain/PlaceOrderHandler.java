package com.example.pattern.chain;

import com.alibaba.fastjson.JSON;
import com.ximalaya.xmkp.trade.channel.bo.OrderResult;
import com.ximalaya.xmkp.trade.channel.service.TradeOrderAndPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:40
 * @description PlaceOrder
 */
@Component
@Slf4j
public class PlaceOrderHandler extends TradeHandler {

    @Resource
    private TradeOrderAndPayService tradeOrderAndPayService;

    @Override
    public void doHandler(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        Map<String, String> map = new HashMap<>(2);
        map.put("ip", context.getIpAddr());
        map.put("userAgent", context.getUserAgent());
        OrderResult result = tradeOrderAndPayService.placeOrder(context.getParam(), context.getReqId(), JSON.toJSONString(map));
        context.setOrderResult(result);
        next.doHandler(context);
    }
}
