package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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


    @Override
    public void doHandler(TradeHandlerContext context) {

        //自动识别填充deviceTypeId
        //checkAndUpdateDeviceTypeId(context);

        //自动识别accessChannel
        //checkAndUpdateAccessChannel(context);

        //自动识别wx openId
        //checkAndUpdateWxOpenId(context);

        next.doHandler(context);
    }
}
