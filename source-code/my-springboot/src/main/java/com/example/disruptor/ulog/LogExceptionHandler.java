package com.example.disruptor.ulog;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rrz
 */
public class LogExceptionHandler implements ExceptionHandler<Object> {
  private static final Logger LOGGER = LoggerFactory.getLogger(LogExceptionHandler.class);
  @Override
  public void handleEventException(Throwable ex, long sequence, Object event) {
    LOGGER.error("process event error ", ex);
  }

  @Override
  public void handleOnStartException(Throwable ex) {
    LOGGER.error("start handle event error ", ex);
  }

  @Override
  public void handleOnShutdownException(Throwable ex) {
    LOGGER.error("shutdown handle event error ", ex);
  }
}
