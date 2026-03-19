package com.lotus.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    public ResultVO() {
    }

    public ResultVO(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResultVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "操作成功");
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }

    public static <T> ResultVO<T> success(String message) {
        return new ResultVO<>(200, message);
    }

    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(200, message, data);
    }

    public static <T> ResultVO<T> error() {
        return new ResultVO<>(500, "操作失败");
    }

    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message);
    }

    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message);
    }

    public static <T> ResultVO<T> unauthorized() {
        return new ResultVO<>(401, "未授权");
    }

    public static <T> ResultVO<T> unauthorized(String message) {
        return new ResultVO<>(401, message);
    }

    public static <T> ResultVO<T> forbidden() {
        return new ResultVO<>(403, "禁止访问");
    }

    public static <T> ResultVO<T> forbidden(String message) {
        return new ResultVO<>(403, message);
    }

    public static <T> ResultVO<T> notFound() {
        return new ResultVO<>(404, "资源不存在");
    }

    public static <T> ResultVO<T> badRequest(String message) {
        return new ResultVO<>(400, message);
    }
}