package com.github.mr.pool;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : Milo
 */
public class MergePool {
    private static final BlockingDeque<RequestPromise> BLOCKING_DEQUE = new LinkedBlockingDeque<>(1000);
    private static final BasicThreadFactory THREAD_FACTORY = new BasicThreadFactory.Builder().namingPattern("scheduled-thread-pool-%d").daemon(true).build();
    private static final ScheduledExecutorService POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1, THREAD_FACTORY);

    static {
        POOL_EXECUTOR.scheduleAtFixedRate(job(), 0, 50, TimeUnit.MILLISECONDS);
    }

    public static void main(String[] args) throws IOException {
        for(int i = 0 ; i < 10000 ; i++) {
            new Thread(()->{
                MergeResponse submit = submit(new MergeRequest());
                if(submit.getSuccess()){
                    System.out.println(submit);
                }
            }).start();
        }
        System.in.read();
    }

    public static MergeResponse submit(MergeRequest mergeRequest) {
        if(BLOCKING_DEQUE.size() >= 1000){
            return new MergeResponse(false);
        }
        // todo 阈值判断
        // todo 队列创建
        RequestPromise requestPromise = new RequestPromise(mergeRequest, null);
        synchronized(requestPromise) {
            if(BLOCKING_DEQUE.size() >= 10){
                return new MergeResponse(false);
            }
            boolean flag = false;
            try {
                flag = BLOCKING_DEQUE.offer(requestPromise, 100, TimeUnit.MILLISECONDS);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if(!flag) {
                return new MergeResponse(false);
            }
            try {
                requestPromise.wait(2000);
                if(requestPromise.getResult() == null) {
                    return new MergeResponse(false);
                }
            } catch(InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
        return requestPromise.getResult();
    }

    public static Runnable job() {
        return () -> {
            ArrayList<RequestPromise> buffer = new ArrayList<>();
            while(!Thread.interrupted()) {
                try {
                    Thread.sleep(100);
                } catch(InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                for(int i = 0 ; i < BLOCKING_DEQUE.size() ; i++) {
                    RequestPromise requestPromise = BLOCKING_DEQUE.poll();
                    if(requestPromise == null) {
                        continue;
                    }
                    buffer.add(requestPromise);
                }

                if(!buffer.isEmpty()) {
                    System.out.println("批量执行开始"+ buffer.size());
                    for(RequestPromise promise : buffer) {
                        synchronized(promise) {
                            promise.setResult(new MergeResponse(true));
                            promise.notify();
                        }
                    }
                    System.out.println("批量执行开始结束");
                }
                buffer.clear();
            }
        };
    }
}

@Data
@AllArgsConstructor
class RequestPromise {
    private MergeRequest mergeRequest;
    private MergeResponse result;
}

@Data
@AllArgsConstructor
class MergeResponse {
    private Boolean success;
}

@Data
@AllArgsConstructor
class MergeRequest implements Serializable {
}

