package com.example.xinli.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Result<T> {
    private Integer code;
    private String msg;  // 改为msg以匹配前端期望
    private T data;
    private LocalDateTime timestamp;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("success");
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(message);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
    
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
}
