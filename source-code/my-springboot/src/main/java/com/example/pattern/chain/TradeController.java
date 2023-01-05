package com.example.pattern.chain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/trade")
public class TradeController {

    @Resource
    private OuterValidateHandler outerValidateHandler;

    @Resource
    private CheckZeroAmountOrderHandler checkZeroAmountOrderHandler;

    @Resource
    private PlaceOrderHandler placeOrderHandler;

    @Resource
    private PayHandler payHandler;

    @Resource
    private GetCouponInfoHandler getCouponInfoHandler;

    @Resource
    private ParamsAutoFillHandler paramsAutoFillHandler;


    /**
     * 下单并直接支付
     * * @param request
     *
     * @return
     */
    @PostMapping(value = "/placeorderandmakepayment")
    public void placeOrderAndMakePayment(HttpServletRequest request) {
        String app = "";
        String ipAddr = "";
        String userAgent = "";
        TradeHandlerContext context = TradeHandlerContext.builder()
                .app(app)
                .ipAddr(ipAddr)
                .userAgent(userAgent)
                .servletRequest(request)
                .handlerStartTime(System.currentTimeMillis()).build();
        TradeHandler.Builder builder = new TradeHandler.Builder();
        builder.addHandler(outerValidateHandler)
                .addHandler(paramsAutoFillHandler)
                .addHandler(getCouponInfoHandler)
                .addHandler(checkZeroAmountOrderHandler)
                .addHandler(placeOrderHandler)
                .addHandler(payHandler);
        builder.build().doHandler(context);


    }

}
