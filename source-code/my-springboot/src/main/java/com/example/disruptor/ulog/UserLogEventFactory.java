package com.example.disruptor.ulog;

import com.lmax.disruptor.EventFactory;

public class UserLogEventFactory implements EventFactory<UserLogEvent> {

	@Override
	public UserLogEvent newInstance() {
		return new UserLogEvent();
	}

}
