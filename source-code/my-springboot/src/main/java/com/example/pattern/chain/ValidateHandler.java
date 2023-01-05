package com.example.pattern.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ximalaya.xmkp.trade.channel.service.ChannelRequestIdGenerateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:32
 * @description Validate
 */
@Component
@Slf4j
public class ValidateHandler extends TradeHandler{

    @Resource
    private ChannelRequestIdGenerateService channelRequestIdGenerateService;

    @Override
    public void doHandler(TradeHandlerContext context) {
        String reqId = channelRequestIdGenerateService.generateChannelRequestId();
        context.setReqId(reqId);
        checkParams(context);
        next.doHandler(context);
    }

    private void checkParams(TradeHandlerContext context){
        checkArgument(ObjectUtil.isNotNull(context.getReqId()), "请求ID不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getChannel()), "下单渠道不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getTradeType()), "交易类型不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getUserId()), "用户ID不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getPayInfo()), "支付信息不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getPayInfo().getPayChannel()), "支付渠道不能为空");
        checkArgument(ObjectUtil.isNotNull(context.getParam().getPayInfo().getAccessChannel()), "访问渠道不能为空");
        checkArgument(CollUtil.isNotEmpty(context.getParam().getItemInfos()), "商品信息列表不能为空");
        context.getParam().getItemInfos().forEach(itemInfo -> {
            checkArgument(ObjectUtil.isNotNull(itemInfo.getItemId()), "商品ID不能为空");
        });
        if (context.getParam().getItemInfos().size() > 1) {
            throw new IllegalArgumentException("暂时只支持单商品下单");
        }
    }
}
