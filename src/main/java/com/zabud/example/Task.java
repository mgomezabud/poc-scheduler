package com.zabud.example;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Task {

  @Scheduled(cron = "0 0/1 * 1/1 * ?")
  public void task1() {
    log.info("task 1 - " + System.currentTimeMillis());
  }

  @Scheduled(cron = "0 0/1 * 1/1 * ?")
  public void task2() {
    log.info("task 2" + System.currentTimeMillis());
  }

  @Bean(destroyMethod = "shutdown")
  public Executor taskExecutor() {
      return Executors.newScheduledThreadPool(100);
  }

}
