package com.example.disruptor.ulog;

import com.lmax.disruptor.EventTranslatorVararg;
import com.lmax.disruptor.RingBuffer;

public class UserLogEventProducer {

	private static final EventTranslatorVararg<UserLogEvent> TRANSLATOR = (UserLogEvent event, long sequence, Object... args) -> {
			event.setUid((int)args[0]);
			event.setUname(args[1].toString());
			event.setTime(args[2].toString());
			event.setType(((UlogType)args[3]).getName());
			event.setBizId(((BizId)args[4]).getCode());
			event.setAfter(args[5].toString());
			event.setRequestUrl(args[6].toString());
			event.setRemark(args[7].toString());
	};
	
	private final RingBuffer<UserLogEvent> ringBuffer;
	
	public UserLogEventProducer(RingBuffer<UserLogEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}
	
	public void publish(int uid,String name,String time,UlogType type,BizId bizId,String after,String requestUrl, String remark) {
		ringBuffer.publishEvent(TRANSLATOR,uid,name,time,type,bizId,after,requestUrl,remark);
	}
}
