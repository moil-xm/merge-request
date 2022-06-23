package com.github.mr.pool;

import java.util.function.Function;

/**
 * @author : Milo
 */
public class Promise {
    private String name;
    private Object params;
    private Object result;
    private Function<Object,Object> process;

    public Object getParams() {
        return params;
    }

    public Object getResult() {
        return result;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Function<Object, Object> getProcess() {
        return process;
    }

    public void setProcess(Function<Object, Object> process) {
        this.process = process;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
