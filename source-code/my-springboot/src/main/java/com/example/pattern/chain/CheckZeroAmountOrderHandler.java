package com.example.pattern.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ximalaya.eros.mainstay.context.annotation.MainstayClient;
import com.ximalaya.mainstay.rpc.thrift.TException;
import com.ximalaya.xmkp.trade.channel.bo.MoreCouponAmount;
import com.ximalaya.xmkp.trade.channel.bo.PlaceOrderAndPayParam;
import com.ximalaya.xmkp.trade.channel.enums.Constant;
import com.ximalaya.xmkp.trade.channel.enums.ItemTypeEnum;
import com.ximalaya.xmkp.trade.channel.enums.MainstayResultCodeEnum;
import com.ximalaya.xmkp.trade.channel.exception.BusinessException;
import com.ximalaya.xmkp.trade.price.api.converter.TCalcPriceRequestConverter;
import com.ximalaya.xmkp.trade.price.api.converter.TCalcPriceResponseConverter;
import com.ximalaya.xmkp.trade.price.api.model.CalcPriceRequest;
import com.ximalaya.xmkp.trade.price.api.model.CalcPriceResponse;
import com.ximalaya.xmkp.trade.price.api.model.ItemInfo;
import com.ximalaya.xmkp.trade.price.api.model.PromotionInfo;
import com.ximalaya.xmkp.trade.price.api.thrift.TCalcPriceResponse;
import com.ximalaya.xmkp.trade.price.api.thrift.TPriceCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gavin.wang
 * @date 2021/12/23 11:36
 * @description 判断0元订单单
 */
@Component
@Slf4j
public class CheckZeroAmountOrderHandler extends TradeHandler {

    @MainstayClient(group = "xmkp-trade-price-group", autoRegisterJavaIface = true)
    private TPriceCommandService.Iface priceCommandService;

    private static final Long ZERO = 0L;

    @Override
    public void doHandler(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        if (ObjectUtil.isNotNull(context.getParam().getPromotionInfos()) && context.getParam().getPromotionInfos().size() > 1) {
            setOptimalCoupon(context.getParam(), context.getReqId());
        } else {
            checkPayAmountIsZero(context.getParam(), context.getReqId());
        }
        next.doHandler(context);
    }

    public void checkPayAmountIsZero(PlaceOrderAndPayParam param, String reqId) {
        CalcPriceRequest req = packCalcPriceRequest(param, reqId);
        Long payAmount = calcPayAmount(req);
        if (ZERO.equals(payAmount)) {
            log.info("this itemId:{} calcPrice payAmount is zero.", param.getItemInfos().get(0));
            param.getPayInfo().setPayChannel(Constant.PAY_CHANNEL);
        }
    }

    public Long calcPayAmount(CalcPriceRequest req) {
        CalcPriceResponse response;
        try {
            long start = System.currentTimeMillis();
            TCalcPriceResponse tResponse = priceCommandService.calcPrice(TCalcPriceRequestConverter.transform(req));
            response = TCalcPriceResponseConverter.transform(tResponse);
            log.info("invoke calcPrice cost {}ms, request:[{}]  response:[{}]", DateUtil.spendMs(start)
                    , JSON.toJSONString(req), JSON.toJSONString(response));
        } catch (TException e) {
            log.error("invoke calcPrice error: ", e);
            throw new BusinessException("调用算价接口【calcPrice】异常：", Constant.SYSTEM_ERROR_MSG);
        }
        if (!MainstayResultCodeEnum.OK.getCode().equals(response.getResultCode())) {
            throw new BusinessException("调用算价接口【calcPrice】异常：" + response.getResultMsg(), Constant.SYSTEM_ERROR_MSG);
        }
        return response.getPayAmount();
    }

    /**
     * 多优惠券时设置最优 优惠券
     *
     * @param param
     * @param reqId
     */
    private void setOptimalCoupon(PlaceOrderAndPayParam param, String reqId) {
        log.info("itemId:{},has more coupons, coupons:[{}]", param.getItemInfos().get(0)
                , JSON.toJSONString(param.getPromotionInfos()));
        List<MoreCouponAmount> moreCouponAmounts = Lists.newArrayList();
        param.getPromotionInfos().forEach(promotionInfo -> {
            CalcPriceRequest req = packCalcPriceRequest(param, promotionInfo, reqId);
            Long payAmount = calcPayAmount(req);
            MoreCouponAmount moreCouponAmount = MoreCouponAmount.builder()
                    .payAmount(payAmount)
                    .promotionInfo(promotionInfo)
                    .build();
            moreCouponAmounts.add(moreCouponAmount);
        });
        List<MoreCouponAmount> list = moreCouponAmounts.stream()
                .sorted(Comparator.comparing(MoreCouponAmount::getPayAmount)).collect(Collectors.toList());
        log.info("moreCouponAmount result:[{}], optimal coupon is [{}]", JSON.toJSONString(list)
                , JSON.toJSONString(list.get(0).getPromotionInfo()));
        param.setPromotionInfos(Lists.newArrayList(list.get(0).getPromotionInfo()));
        if (ZERO.equals(list.get(0).getPayAmount())) {
            param.getPayInfo().setPayChannel(Constant.PAY_CHANNEL);
        }
    }

    private CalcPriceRequest packCalcPriceRequest(PlaceOrderAndPayParam param
            , com.ximalaya.xmkp.trade.channel.bo.PromotionInfo promotionInfo, String reqId) {
        CalcPriceRequest req = new CalcPriceRequest();
        req.setChannel(param.getChannel());
        req.setReqId(reqId);
        req.setSource(Constant.SOURCE);
        req.setUserId(param.getUserId());
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setItemId(param.getItemInfos().get(0).getItemId());
        itemInfo.setItemType(ItemTypeEnum.FICTITIOUS.getType());
        itemInfo.setQuantity(1);
        req.setItemInfos(Arrays.asList(itemInfo));
        if (CollUtil.isEmpty(param.getPromotionInfos())) {
            return req;
        }
        List<PromotionInfo> promotionInfos = Lists.newArrayList();
        PromotionInfo promotion = new PromotionInfo();
        promotion.setPromotionCode(promotionInfo.getPromotionCode());
        promotion.setPromotionId(promotionInfo.getPromotionId());
        promotion.setPromotionType(promotionInfo.getPromotionType());
        promotionInfos.add(promotion);
        req.setPromotionInfos(promotionInfos);
        return req;
    }

    private CalcPriceRequest packCalcPriceRequest(PlaceOrderAndPayParam param, String reqId) {
        CalcPriceRequest req = new CalcPriceRequest();
        req.setChannel(param.getChannel());
        req.setReqId(reqId);
        req.setSource(Constant.SOURCE);
        req.setUserId(param.getUserId());
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.setItemId(param.getItemInfos().get(0).getItemId());
        itemInfo.setItemType(ItemTypeEnum.FICTITIOUS.getType());
        itemInfo.setQuantity(1);
        req.setItemInfos(Arrays.asList(itemInfo));
        if (CollUtil.isEmpty(param.getPromotionInfos())) {
            return req;
        }
        List<PromotionInfo> promotionInfos = Lists.newArrayList();
        param.getPromotionInfos().forEach(promotionInfo -> {
            PromotionInfo promotion = new PromotionInfo();
            promotion.setPromotionCode(promotionInfo.getPromotionCode());
            promotion.setPromotionId(promotionInfo.getPromotionId());
            promotion.setPromotionType(promotionInfo.getPromotionType());
            promotionInfos.add(promotion);
        });
        req.setPromotionInfos(promotionInfos);
        return req;
    }
}
