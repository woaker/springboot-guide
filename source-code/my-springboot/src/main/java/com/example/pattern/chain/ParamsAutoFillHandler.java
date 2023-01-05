package com.example.pattern.chain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ximalaya.business.common.lib.utils.UserAgentUtil;
import com.ximalaya.xmkp.trade.channel.bo.PayInfo;
import com.ximalaya.xmkp.trade.channel.config.FootballConfigBean;
import com.ximalaya.xmkp.trade.channel.util.EnvUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author wumch
 * @Description: 参数自动识别和填充
 * @date 2022/1/21 17:12
 */
@Component
@Slf4j
public class ParamsAutoFillHandler extends TradeHandler {

    private static final String DEVICE_TYPE_ID_KEY = "deviceTypeId";

    private static final Integer DEVICE_TYPE_ID_ANDROID = 100;

    private static final Integer DEVICE_TYPE_ID_IOS = 200;

    //private static final Integer DEVICE_TYPE_ID_WEB = 300;

    @Resource
    private EnvUtils envUtils;

    @Resource
    private FootballConfigBean footballConfigBean;

    @Override
    public void doHandler(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {

        //自动识别填充deviceTypeId
        checkAndUpdateDeviceTypeId(context);

        //自动识别accessChannel
        checkAndUpdateAccessChannel(context);

        //自动识别wx openId
        checkAndUpdateWxOpenId(context);

        next.doHandler(context);
    }

    private void checkAndUpdateDeviceTypeId(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        Map map = StringUtils.isBlank(context.getParam().getContext()) ? new HashMap() : JSON.parseObject(context.getParam().getContext(), Map.class);
        if (Objects.isNull(map.get(DEVICE_TYPE_ID_KEY))) {
            Integer deviceTypeId = checkDeviceType(context);
            if (Objects.nonNull(deviceTypeId)) {
                log.info("old context:{}", context.getParam().getContext());
                map.put(DEVICE_TYPE_ID_KEY, deviceTypeId);
                context.getParam().setContext(JSON.toJSONString(map));
                log.info("new context:{}", context.getParam().getContext());
            }
        }
    }

    private Integer checkDeviceType(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        String agent = Objects.isNull(context.getServletRequest()) ? null : context.getServletRequest().getHeader("user-agent");
        log.info("agent:{}", agent);
        if (StringUtils.isNotBlank(agent)) {
            if (agent.contains("Android") || agent.contains("Linux")) {
                return DEVICE_TYPE_ID_ANDROID;
            } else if (agent.contains("iPhone") || agent.contains("iPod") || agent.contains("iPad")) {
                return DEVICE_TYPE_ID_IOS;
            } /*else if(agent.contains("micromessenger")){
            //wx
            } else {
                //pc
            }*/
        }
        return null;
    }

    private void checkAndUpdateAccessChannel(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        Integer oldAccessChannel = null;

        if (Objects.nonNull(context.getParam()) && Objects.nonNull(context.getParam().getPayInfo())) {
            oldAccessChannel = context.getParam().getPayInfo().getAccessChannel();

            if (Objects.isNull(oldAccessChannel) || oldAccessChannel.equals(Integer.valueOf(2))) { //2: H5，m站兼容处理，后续推动m站传空值，服务端自动填充
                log.info("old access channel:{}", oldAccessChannel);
                Integer accessChannel = 2; // H5
                if (UserAgentUtil.isInWeixin(context.getServletRequest())) {
                    accessChannel = 4; //JS
                } else if (UserAgentUtil.isInApp(context.getServletRequest())) {
                    accessChannel = 3; //SDK
                }
                log.info("new access channel:{}", accessChannel);

                if (Objects.isNull(oldAccessChannel) || !oldAccessChannel.equals(accessChannel)) {
                    context.getParam().getPayInfo().setAccessChannel(accessChannel);
                }
            }

        }
        //外部应用过来都为
        // 站外分销渠道
        //COMMON(7, "通用"),
        if (StringUtils.isNotEmpty(context.getApp())) {
            PayInfo payInfo = new PayInfo();
            //外部过来统统 1-线下支付
            payInfo.setPayChannel(1);
            payInfo.setAccessChannel(7);
            context.getParam().setPayInfo(payInfo);
        }
    }

    private void checkAndUpdateWxOpenId(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        // 6：m站，当前只对M站做自动识别openId
        if (Objects.nonNull(context.getParam()) && Integer.valueOf(6).equals(context.getParam().getChannel())) {
            if (StringUtils.isBlank(context.getParam().getOpenId())) {
                String openId = checkWxOpenId(context);
                if (StringUtils.isNotBlank(openId)) {
                    context.getParam().setOpenId(openId);
                }
            }
        }
        //M站H5结算页去除context中openeId
        if (Objects.nonNull(context.getParam().getContext()) &&
                !context.getParam().getContext().isEmpty() &&
                Integer.valueOf(6).equals(context.getParam().getChannel()) &&
                Integer.valueOf(5).equals(context.getParam().getPayInfo().getPayChannel())) {
            log.info("old pay context:{}", JSON.toJSONString(context.getParam().getContext()));
            Map<String, Object> map = JSON.parseObject(context.getParam().getContext(), Map.class);
            map.entrySet().removeIf(stringObjectEntry -> stringObjectEntry.getKey().equals("openId"));
            log.info("new pay context:{}", JSON.toJSONString(map));
            context.getParam().setContext(JSON.toJSONString(map));
        }
    }

    private String checkWxOpenId(com.ximalaya.xmkp.trade.channel.tradehandler.TradeHandlerContext context) {
        String openID = null;

        try {
            if (StringUtils.isNotBlank(footballConfigBean.getChannelToThirdpartyIdsMapping())) {
                if (Objects.nonNull(context.getParam()) && Objects.nonNull(context.getParam().getChannel())) {
                    log.info("checkWxOpenId dict: {}", footballConfigBean.getChannelToThirdpartyIdsMapping());
                    JSONArray channelMapping = JSON.parseArray(footballConfigBean.getChannelToThirdpartyIdsMapping());
                    Integer channel = context.getParam().getChannel();

                    Integer thirdpartyId = null;
                    for (Object item : channelMapping) {
                        if (channel.equals(((JSONObject) item).getInteger("channel"))) {
                            thirdpartyId = ((JSONObject) item).getInteger("thirdpartyId");
                            break;
                        }
                    }
                    log.info("checkWxOpenId thirdpartyId:{}", thirdpartyId);

                    if (Objects.nonNull(thirdpartyId)) {
                        Integer environmentId = envUtils.isProd() ? 1 : 4; //1:线上   4:测试
                        String key = "wxopenid_" + thirdpartyId + "_" + environmentId + "_secure";
                        log.info("checkWxOpenId key:{}", key);

                        if (null != context.getServletRequest().getCookies()) {
                            Cookie[] cookies = context.getServletRequest().getCookies();
                            int cookiesLength = cookies.length;

                            for (int i = 0; i < cookiesLength; ++i) {
                                Cookie c = cookies[i];
                                if (c.getName().equals(key)) {
                                    String cookieValue = c.getValue();
                                    log.info("checkWxOpenId cookie value: {}", cookieValue);
                                    openID = cookieValue.split("&")[0];
                                    break;
                                }
                            }
                        }
                        log.info("checkWxOpenId openId:{}", openID);
                    }

                }
            }
        } catch (Exception e) {
            log.error("checkWxOpenId error", e);
        }

        return openID;
    }

}
