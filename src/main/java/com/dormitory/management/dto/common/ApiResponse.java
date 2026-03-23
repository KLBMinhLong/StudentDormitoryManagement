package com.dormitory.management.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int code;
    private String message;
    private T result;

    public static <T> ApiResponse<T> success(int code, String message, T result) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .result(result)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .result(null)
                .build();
    }
}
