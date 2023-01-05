package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:36
 * @description 判断0元订单单
 */
@Component
@Slf4j
public class CheckZeroAmountOrderHandler extends TradeHandler {

    @Override
    public void doHandler(TradeHandlerContext context) {
/*        if (ObjectUtil.isNotNull(context.getParam().getPromotionInfos()) && context.getParam().getPromotionInfos().size() > 1) {
            setOptimalCoupon(context.getParam(), context.getReqId());
        } else {
            checkPayAmountIsZero(context.getParam(), context.getReqId());
        }*/
        next.doHandler(context);
    }
}
