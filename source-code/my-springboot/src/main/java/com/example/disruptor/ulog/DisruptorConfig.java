package com.example.disruptor.ulog;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * @author rrz
 */
@Configuration
public class DisruptorConfig {

  @Bean(initMethod = "start", destroyMethod = "shutdown")
  public Disruptor<UserLogEvent> requestDisruptor() {
    Disruptor<UserLogEvent> disruptor = new Disruptor<UserLogEvent>(
        new com.ximalaya.xxm.operate.core.disruptor.UserLogEventFactory(), 2048, Executors.defaultThreadFactory(), ProducerType.MULTI,
        new BlockingWaitStrategy());

    disruptor.setDefaultExceptionHandler(new LogExceptionHandler());
    disruptor.handleEventsWithWorkerPool(new UserLogEventHandler());
    return disruptor;
  }
  
  @Bean
  public UserLogEventProducer userLogEventProducer() {
	  UserLogEventProducer producer = new UserLogEventProducer(requestDisruptor().getRingBuffer());
    return producer;
  }

}
