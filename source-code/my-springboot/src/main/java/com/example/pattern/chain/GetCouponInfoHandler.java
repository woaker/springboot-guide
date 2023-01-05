package com.example.pattern.chain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ximalaya.business.gateway.open.sdk.java.common.model.GatewayClientRequestDto;
import com.ximalaya.business.gateway.open.sdk.java.common.model.GatewayClientResponseDto;
import com.ximalaya.business.gateway.open.sdk.java.common.service.CommonClientService;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.base.CouponDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.base.UserCouponDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.request.AllocateCouponV2RequestDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.request.QueryCouponsRequestDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.request.QueryUserAvailableCouponsRequestDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.response.AllocateCouponV2ResponseDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.response.QueryCouponsResponseDto;
import com.ximalaya.business.gateway.open.sdk.java.promotion.api.dto.response.QueryUserAvailableCouponsResponseDto;
import com.ximalaya.eros.mainstay.context.annotation.MainstayClient;
import com.ximalaya.mainstay.rpc.thrift.TException;
import com.ximalaya.xmkp.edu.api.thrift.TEduSemesterBaseInfoDTO;
import com.ximalaya.xmkp.edu.api.thrift.TEduSemesterService;
import com.ximalaya.xmkp.trade.channel.bo.ItemInfo;
import com.ximalaya.xmkp.trade.channel.bo.PromotionInfo;
import com.ximalaya.xmkp.trade.channel.config.FootballConfigBean;
import com.ximalaya.xmkp.trade.channel.enums.Constant;
import com.ximalaya.xmkp.trade.channel.enums.ItemIdTypeEnum;
import com.ximalaya.xmkp.trade.channel.exception.BusinessException;
import com.ximalaya.xmkp.trade.channel.util.OuterItemsCouponIdsConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author gavin.wang
 * @date 2021/12/29 11:15
 * @description 获取优惠券信息
 */
@Component
@Slf4j
public class GetCouponInfoHandler extends TradeHandler {

    @Autowired
    private CommonClientService commonClientService;

    @Resource
    private FootballConfigBean footballConfigBean;

    @Autowired
    private OuterItemsCouponIdsConfigUtils outerItemsCouponIdsConfigUtils;

    @MainstayClient(group = "xmkp-edu", autoRegisterJavaIface = true, timeout = "6000")
    private TEduSemesterService.Iface eduSemesterService;

    @Override
    public void doHandler(TradeHandlerContext context) {
        if (CollUtil.isNotEmpty(context.getParam().getPromotionInfos())) {
            List<UserCouponDto> couponDtos = queryCouponsByUserId(context.getParam().getUserId(), context.getParam().getItemInfos().get(0).getItemId());
            if (CollUtil.isEmpty(couponDtos)) {
                context.getParam().setPromotionInfos(null);
            } else {
                context.getParam().getPromotionInfos().forEach(promotionInfo -> {
                    selectCouponById(couponDtos, promotionInfo, null);
                });
                checkReceivePromotionCode(context);
            }
        }
        //sdk过来零元单
        if (StringUtils.isNotEmpty(context.getApp()) && context.getApp().equals(Constant.SDK_APP_ID)) {
            List<Long> campIds = new ArrayList<>();
            //campId查询优惠券
            context.getParam().getItemInfos().stream().forEach(itemInfo -> {
                if (itemInfo.getItemIdType().equals(ItemIdTypeEnum.CAMP.getType())) {
                    campIds.add(itemInfo.getItemId());
                }
            });
            List<PromotionInfo> promotionInfoList = new ArrayList<>();
            List<Long> couponIds = new ArrayList<>();
            campIds.stream().forEach(campId -> {
                Long couponId = outerItemsCouponIdsConfigUtils.getByCampId(campId);
                if (Objects.isNull(couponId)) {
                    throw new BusinessException("SDK过来的下单camp:" + campId + "，未配置相应优惠券ID，请及时处理!");
                } else {
                    couponIds.add(couponId);
                }
            });
            HashSet<Long> couponIdsSet = new HashSet<>(couponIds);
            //查券
            Map<Long, CouponDto> couponDtoMap = queryCoupons(couponIdsSet);
            //领券
            if (CollUtil.isNotEmpty(couponDtoMap)) {
                CouponDto couponDto = couponDtoMap.get(couponIds.get(0));
                if (Objects.nonNull(couponDto)) {
                    Long promotionCode = allocateCouponV2(context.getParam().getUserId(), couponIds.get(0));
                    if (Objects.nonNull(promotionCode)) {
                        PromotionInfo promotionInfo = new PromotionInfo();
                        promotionInfo.setPromotionId(couponDto.getCouponId());
                        promotionInfo.setPromotionCode(promotionCode);
                        promotionInfo.setPromotionType(couponTypeToPromotionType(couponDto.getCouponType()));
                        promotionInfo.setDiscountType(couponDto.getDiscountType());
                        promotionInfoList.add(promotionInfo);
                    }
                }
            }
            context.getParam().setPromotionInfos(promotionInfoList);
        }
        campIdToItemId(context);
        next.doHandler(context);
    }

    /**
     * couponType 优惠券类型,1/优惠券,2/代金券
     * promotionType
     * PROMOTION_PACKAGE(1, "打包购买"),
     * COUPON(2, "优惠券"),
     * GROUPON(3, "拼团"),
     * VOUCHER(4, "代金券"),
     * TIMED_DISCOUNT(5, "限时折扣"),
     * GIFT(6,"买送");
     *
     * @param couponType
     * @return
     */
    private int couponTypeToPromotionType(int couponType) {
        if (couponType == 1) {
            return 2;
        }
        if (couponType == 2) {
            return 4;
        }
        return 2;
    }

    private void checkReceivePromotionCode(TradeHandlerContext context) {
        if (CollUtil.isEmpty(context.getParam().getPromotionInfos())) {
            return;
        }
        List<PromotionInfo> promotionInfos = Lists.newArrayList();
        for (PromotionInfo promotionInfo : context.getParam().getPromotionInfos()) {
            if (ObjectUtil.isNotNull(promotionInfo.getPromotionCode())) {
                promotionInfos.add(promotionInfo);
            }
        }
        context.getParam().setPromotionInfos(promotionInfos);
    }

    /**
     * 根据优惠券ID选择优惠券
     *
     * @param couponDtos    优惠券列表
     * @param promotionInfo
     * @param app           应用标识
     */
    private void selectCouponById(List<UserCouponDto> couponDtos, PromotionInfo promotionInfo, String app) {
        if (ObjectUtil.isNull(promotionInfo.getPromotionId()) || CollUtil.isEmpty(couponDtos)) {
            return;
        }
        boolean flag = false;
        for (UserCouponDto userCouponDto : couponDtos) {
            if (userCouponDto.getCouponId().equals(promotionInfo.getPromotionId())) {
                promotionInfo.setPromotionCode(userCouponDto.getPromoCode());
                if (StringUtils.isNotBlank(app) && app.equals(Constant.SDK_APP_ID)) {
                    promotionInfo.setPromotionType(userCouponDto.getCouponType());
                    promotionInfo.setDiscountType(userCouponDto.getCoupon().getDiscountType());
                }
                flag = true;
                break;
            }
        }
        if (!flag) {
            promotionInfo.setPromotionCode(null);
        }
    }

    private List<UserCouponDto> queryCouponsByUserId(Long userId, Long itemId) {
        QueryUserAvailableCouponsRequestDto requestDto = new QueryUserAvailableCouponsRequestDto();
        requestDto.setUserId(userId);
        requestDto.setItemId(itemId);
        requestDto.setDomain(1);
        GatewayClientRequestDto<QueryUserAvailableCouponsRequestDto> request = new GatewayClientRequestDto<>(
                footballConfigBean.getBusinessTradeAppKey(), footballConfigBean.getBusinessTradeAppSecret(), requestDto);
        long start = System.currentTimeMillis();
        GatewayClientResponseDto<QueryUserAvailableCouponsResponseDto> response = commonClientService.execute(request);
        log.info("query main station queryUserAvailableCouponsByItemId cost:{}ms, response:{}"
                , DateUtil.spendMs(start), JSON.toJSONString(response));
        if (!response.success()) {
            throw new BusinessException("调用主站查询商品优惠券接口【queryUserAvailableCouponsByItemId】异常："
                    , Constant.SYSTEM_ERROR_MSG);
        }
        return response.getData().getUserCouponDtoList();
    }

    /**
     * 批量查询优惠券
     *
     * @param couponIds
     * @return
     */
    private Map<Long, CouponDto> queryCoupons(Set<Long> couponIds) {
        QueryCouponsRequestDto queryCouponsRequestDto = new QueryCouponsRequestDto();
        queryCouponsRequestDto.setCouponIds(couponIds);
        GatewayClientRequestDto<QueryCouponsRequestDto> request = new GatewayClientRequestDto<>(
                footballConfigBean.getBusinessTradeAppKey(), footballConfigBean.getBusinessTradeAppSecret(), queryCouponsRequestDto);
        // invoke remote
        log.info("request queryCoupons:{}", JSON.toJSONString(request));
        long start = System.currentTimeMillis();
        GatewayClientResponseDto<QueryCouponsResponseDto> gatewayClientResponseDto = commonClientService.execute(request);
        long end = System.currentTimeMillis();
        log.info("response queryCoupons:{} cost:{}ms", JSON.toJSONString(gatewayClientResponseDto), (end - start));
        // handle GatewayClientResponseDto
        if (gatewayClientResponseDto.success()) {
            return gatewayClientResponseDto.getData().getCouponDtoMap();
        } else {
            throw new BusinessException("调用主站批量查询优惠券接口【queryCoupons】异常："
                    , Constant.SYSTEM_ERROR_MSG);
        }
    }

    /**
     * 领取单张优惠券（返回券码）
     *
     * @param userId
     * @param couponId
     * @return
     */
    private Long allocateCouponV2(Long userId, Long couponId) {
        AllocateCouponV2RequestDto allocateCouponV2RequestDto = new AllocateCouponV2RequestDto();
        allocateCouponV2RequestDto.setUserId(userId);
        allocateCouponV2RequestDto.setDomain(1);
        allocateCouponV2RequestDto.setCouponId(couponId);
        GatewayClientRequestDto<AllocateCouponV2RequestDto> request = new GatewayClientRequestDto<>(
                footballConfigBean.getBusinessTradeAppKey(), footballConfigBean.getBusinessTradeAppSecret(), allocateCouponV2RequestDto);
        // invoke remote
        log.info("request allocateCouponV2:{}", JSON.toJSONString(request));
        long start = System.currentTimeMillis();
        GatewayClientResponseDto<AllocateCouponV2ResponseDto> gatewayClientResponseDto = commonClientService.execute(request);
        long end = System.currentTimeMillis();
        log.info("response allocateCouponV2:{} cost:{}ms", JSON.toJSONString(gatewayClientResponseDto), (end - start));
        // handle GatewayClientResponseDto
        if (gatewayClientResponseDto.success()) {
            return gatewayClientResponseDto.getData().getPromoCode();
        } else {
            throw new BusinessException("调用主站领取单张优惠券（返回券码）接口【allocateCouponV2】异常："
                    , Constant.SYSTEM_ERROR_MSG);
        }
    }

    /**
     * campId to itemId
     *
     * @param context
     */
    private void campIdToItemId(TradeHandlerContext context) {
        //campId转换
        List<ItemInfo> itemInfoList = new ArrayList<>();
        context.getParam().getItemInfos().stream().forEach(itemInfo -> {
            if (Objects.nonNull(itemInfo.getItemIdType()) && itemInfo.getItemIdType().equals(ItemIdTypeEnum.CAMP.getType())) {
                itemInfo.setItemId(getSemesterRef(itemInfo.getItemId()));
            }
            itemInfoList.add(itemInfo);
        });
        context.getParam().setItemInfos(itemInfoList);
    }

    /**
     * 通过campId拿到在售期
     *
     * @param campId
     * @return
     */
    private long getSemesterRef(long campId) {
        log.info("通过campId={}查询skuRef", campId);
        try {
            TEduSemesterBaseInfoDTO onSaleSemester = eduSemesterService.findOnSaleSemester(campId, System.currentTimeMillis());
            log.info("通过campId={}查询onSaleSemester={}", campId, JSON.toJSONString(onSaleSemester));
            return onSaleSemester.getSkuRef();
        } catch (TException e) {
            log.error("通过campId={}查询skuRef异常", campId, e);
            throw new BusinessException("调用edu通过campId拿到在售期接口【getSemesterRef】异常："
                    , Constant.SYSTEM_ERROR_MSG);
        }
    }
}
