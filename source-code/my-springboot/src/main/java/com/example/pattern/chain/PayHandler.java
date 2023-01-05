package com.example.pattern.chain;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.ximalaya.xmkp.trade.channel.bo.PayResult;
import com.ximalaya.xmkp.trade.channel.enums.Constant;
import com.ximalaya.xmkp.trade.channel.service.TradeOrderAndPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:40
 * @description PlaceOrder
 */
@Component
@Slf4j
public class PayHandler extends TradeHandler {

    @Resource
    private TradeOrderAndPayService tradeOrderAndPayService;

    @Override
    public void doHandler(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        //todo 外研社走线下
        if (StringUtils.isNotEmpty(context.getApp()) && context.getApp().equals(Constant.SDK_APP_ID)) {
            context.getParam().getPayInfo().setPayChannel(Constant.PAY_CHANNEL);
        }
        PayResult result = tradeOrderAndPayService
                .pay(context.getOrderResult().getOrderId(), context.getParam(), context.getReqId(), context.getIpAddr());
        context.setPayResult(result);
        log.info("placeOrderAndPay end, cost {}ms, response order result: [{}], pay result:[{}]."
                , DateUtil.spendMs(context.getHandlerStartTime()), JSON.toJSONString(context.getOrderResult())
                , JSON.toJSONString(context.getPayResult()));
    }
}
