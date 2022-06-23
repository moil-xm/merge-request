package com.github.mr.pool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : Milo
 */
public class MergeProperties {
    private final List<Config> configs = new ArrayList<>();

    public List<Config> getConfigs() {
        return configs;
    }

    public static class Config {
        /**
         * 合并名称
         */
        private String name;
        /**
         * 最大等待时间
         */
        private Duration maxWaitTime = Duration.ofMillis(200);
        /**
         * 合并频率
         */
        private Duration interval = Duration.ofMillis(10);
        /**
         * 最大合并数量
         */
        private int maxMergeCount = 100;
        /**
         * 最小合并数量
         */
        private int minMergeCount = 5;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Duration getMaxWaitTime() {
            return maxWaitTime;
        }

        public void setMaxWaitTime(Duration maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
        }

        public int getMaxMergeCount() {
            return maxMergeCount;
        }

        public void setMaxMergeCount(int maxMergeCount) {
            this.maxMergeCount = maxMergeCount;
        }

        public int getMinMergeCount() {
            return minMergeCount;
        }

        public void setMinMergeCount(int minMergeCount) {
            this.minMergeCount = minMergeCount;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }
    }
}
