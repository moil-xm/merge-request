package com.github.mr.pool;

import java.io.IOException;

/**
 * @author : Milo
 * @description :
 */
public class Test {
    public static void main(String[] args) throws IOException {
        MergeConfiguration mergeConfiguration = new MergeConfiguration();
        for(int i = 0 ; i < 100 ; i++) {
            mergeConfiguration.submit("order", "order" + i, o -> o + "-success");
        }
        while(true){}
    }
}
