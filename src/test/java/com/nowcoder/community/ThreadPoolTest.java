package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    //JDK普通线程池
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    //JDK可定时执行任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    //Spring普通线程池
    @Resource
    private ThreadPoolTaskExecutor threadPoolExecutor;
    //Spring定时任务线程池
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Resource
    AlphaService alphaService;

    private void sleep(long m){
        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello executorService");
            }
        };

        for (int i = 0; i < 10 ;i++){
            executorService.submit(task);
        }

        sleep(10000);
    }

    @Test
    public void testScheduledExecutorService(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ScheduledExecutorService");
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(task,10000,1000, TimeUnit.MILLISECONDS);

        sleep(30000);
    }

    //测试spring普通线程池
    @Test
    public void testThreadPoolTaskExecutor(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolExecutor");
            }
        };

        for (int i = 0; i<10;i++){
            threadPoolExecutor.submit(task);
        }
        sleep(10000);
    }

    //测试Spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler(){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("hello ThreadPoolTaskScheduler");
            }
        };
        Date time = new Date(System.currentTimeMillis()+10000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task,time,1000);
        sleep(30000);
    }


    //Spring普通线程池的简化
    @Test
    public void testThreadPoolTaskExecutorSimple(){
        for (int i = 0;i<10;i++){
            alphaService.execute1();
        }

        sleep(10000);
    }
    @Test
    public void testThreadPoolTaskSchedulerSimple(){

        sleep(30000);
    }




}
