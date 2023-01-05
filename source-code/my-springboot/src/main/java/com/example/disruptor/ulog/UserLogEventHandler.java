package com.example.disruptor.ulog;

import com.lmax.disruptor.WorkHandler;
import com.ximalaya.xxm.common.exception.XxmBaseException;
import com.ximalaya.xxm.common.exception.code.ServiceName;
import com.ximalaya.xxm.common.exception.code.XxmOperateErrorCode;
import com.ximalaya.xxm.operate.core.bo.UserLogBO;
import com.ximalaya.xxm.operate.core.result.InsertResult;
import com.ximalaya.xxm.operate.core.service.CoreUserLogService;
import com.ximalaya.xxm.operate.core.service.CoreXQLService;
import com.ximalaya.xxm.operate.core.util.BOBuilder;
import com.ximalaya.xxm.operate.core.util.ReportSecurityLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Slf4j
public class UserLogEventHandler implements WorkHandler<com.ximalaya.xxm.operate.core.disruptor.UserLogEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserLogEventHandler.class);

	@Autowired
	private CoreUserLogService userLogService;

	@Autowired
	private CoreXQLService xqlService;

	@Autowired
	private ReportSecurityLogUtil reportSecurityLogUtil;

	@Override
	public void onEvent(com.ximalaya.xxm.operate.core.disruptor.UserLogEvent event) throws Exception {
		try {
			/** 解决bean无法注入子线程的问题 **/
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			LOGGER.info("onEvent ! ");
			UserLogBO userLogBO = BOBuilder.userLogEvent2BO(event);
			InsertResult insertResult = userLogService.insertUserLog(userLogBO);
			if(insertResult.getCount() > 0) {
				userLogBO.setId(insertResult.getId());
				xqlService.insertXDCS(userLogBO);

				// 安全审核上报
				reportSecurity(userLogBO);
			}
		} catch (Exception e) {
			LOGGER.error(" service error ! onEvent",UserLogEventHandler.class,e);
			throw new XxmBaseException(ServiceName.XXM_OPERATE,XxmOperateErrorCode.SYSTEM_ERROR,"system error",e);
		}

	}

	private void reportSecurity(UserLogBO userLogBO) {
//		(String bizname, String bizcode, String biztype, String userName, String optPath, String remark
		reportSecurityLogUtil.reportSecurityLog(userLogBO.getType(),String.valueOf( userLogBO.getBizId()),String.valueOf( userLogBO.getBizId()),userLogBO.getUname(),
				userLogBO.getRequestUrl(),
				userLogBO.getRemark(),String.valueOf(userLogBO.getUid()));
	}

}
