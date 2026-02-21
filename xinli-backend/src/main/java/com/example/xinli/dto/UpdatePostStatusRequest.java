package com.example.xinli.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class UpdatePostStatusRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    private String reason;
}
