package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author gavin.wang
 * @date 2021/12/29 11:15
 * @description 获取优惠券信息
 */
@Component
@Slf4j
public class GetCouponInfoHandler extends TradeHandler {


    @Override
    public void doHandler(TradeHandlerContext context) {

        campIdToItemId(context);
        next.doHandler(context);
    }


    /**
     * campId to itemId
     *
     * @param context
     */
    private void campIdToItemId(TradeHandlerContext context) {

    }

}
