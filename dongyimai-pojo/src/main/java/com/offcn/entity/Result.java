package com.offcn.entity;

import java.io.Serializable;
/**
 * 返回结果封装
 */
public class Result implements Serializable {
    //执行是否成功
    private boolean success;
    //执行之后显示的信息
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
