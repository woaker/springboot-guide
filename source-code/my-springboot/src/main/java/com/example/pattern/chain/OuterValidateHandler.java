package com.example.pattern.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ximalaya.xmkp.trade.channel.service.ChannelRequestIdGenerateService;
import com.ximalaya.xmkp.trade.channel.service.QueryTradeOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @description:
 * @author: johnvon.feng@ximalaya.com
 * @date: 2022-05-27 23:00
 **/
@Slf4j
@Component
public class OuterValidateHandler extends TradeHandler {

    @Resource
    private ChannelRequestIdGenerateService channelRequestIdGenerateService;

    @Autowired
    private QueryTradeOrderInfoService queryTradeOrderInfoService;

    @Override
    public void doHandler(TradeHandlerContext context) {
        String reqId = channelRequestIdGenerateService.generateChannelRequestId();
        context.setReqId(reqId);
        checkParams(context);
        next.doHandler(context);
    }

    private void checkParams(TradeHandlerContext context) {
        checkArgument(ObjectUtil.isNotNull(context.getReqId()), "请求ID不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getChannel()), "下单渠道不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getTradeType()), "交易类型不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getUserId()), "用户ID不能为空");
        checkArgument(CollUtil.isNotEmpty(context.getParam().getItemInfos()), "商品信息列表不能为空");
        context.getParam().getItemInfos().forEach(itemInfo -> {
            checkArgument(ObjectUtil.isNotNull(itemInfo.getItemId()), "商品ID不能为空");
            checkArgument(ObjectUtil.isNotNull(itemInfo.getItemIdType()), "商品ID类型不能为空");
        });
        if (context.getParam().getItemInfos().size() > 1) {
            throw new IllegalArgumentException("暂时只支持单商品下单");
        }
    }
}
