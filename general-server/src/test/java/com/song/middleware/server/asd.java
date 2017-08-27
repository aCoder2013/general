package com.song.middleware.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by song on 2017/8/21.
 */
public class asd {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(
                (t1, e) -> LOGGER.error(t1 + " throws exception: " + e));
            return t;
        });
        threadPool.submit(() -> {
            Object obj = null;
            System.out.println(obj.toString());
        });
        threadPool.execute(() -> {
            Object obj = null;
            System.out.println(obj.toString());
        });
        Thread.sleep(10000);
    }


}
