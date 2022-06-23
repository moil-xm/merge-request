package com.github.mr.pool;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author : Milo
 */
@SuppressWarnings("unused")
public class MergeConfiguration {
    private final MergeProperties mergeProperties;
    private final Map<String, BlockingDeque<Promise>> mergeDeque = new HashMap<>();
    private final ScheduledExecutorService mergeScheduled;

    public MergeConfiguration() {
        MergeProperties.Config order = new MergeProperties.Config();
        order.setName("order");
        MergeProperties.Config customer = new MergeProperties.Config();
        customer.setName("customer");
        this.mergeProperties = new MergeProperties();
        mergeProperties.getConfigs().add(order);
        mergeProperties.getConfigs().add(customer);

        BasicThreadFactory factory = new BasicThreadFactory.Builder().namingPattern("scheduled-merge-%d").daemon(true).build();
        this.mergeScheduled = new ScheduledThreadPoolExecutor(this.mergeProperties.getConfigs().size(), factory);
        this.init();
    }

    public synchronized void init() {
        for(MergeProperties.Config config : this.mergeProperties.getConfigs()) {
            BlockingDeque<Promise> promisesQueue = new LinkedBlockingDeque<>(config.getMaxMergeCount());
            mergeDeque.put(config.getName(), promisesQueue);
            mergeScheduled.scheduleAtFixedRate(mergeConsumer(config.getName(), config.getMaxMergeCount()), 0, config.getInterval().toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings("all")
    public Object submit(String name, Object obj, Function<Object, Object> process) {
        Promise promise = new Promise();
        promise.setParams(obj);
        promise.setName(name);
        promise.setProcess(process);
        synchronized(promise) {
            BlockingDeque<Promise> promiseDeque = mergeDeque.get(name);
            if(promiseDeque.offer(promise)) {
                return promise.getProcess().apply(promise.getParams());
            }
            try {
                promise.wait(200);
                if(Objects.isNull(promise.getResult())) {
                    return promise.getProcess().apply(promise.getParams());
                }
            } catch(InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
        return promise.getResult();
    }

    @SuppressWarnings("all")
    private Runnable mergeConsumer(String name, int maxMergeCount) {
        return () -> {
            BlockingDeque<Promise> deque = mergeDeque.get(name);
            List<Promise> promises = new ArrayList<>();
            for(int i = 0, max = Math.min(deque.size(), maxMergeCount) ; i < max ; i++) {
                Promise promise = deque.poll();
                if(Objects.nonNull(promise)) {
                    promises.add(promise);
                }
            }
            if(promises.isEmpty()) {
                return;
            }
            promises.forEach(promise -> {
                promise.setResult(promise.getProcess().apply(promise.getParams()));
                synchronized(promise) {
                    promise.notify();
                }
            });
        };
    }
}
