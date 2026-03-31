package org.ganjp.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Status status;
    private T data;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private int code;
        private String message;
        private Object errors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private String serverDateTime;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status(Status.builder()
                        .code(200)
                        .message(message)
                        .build())
                .data(data)
                .meta(Meta.builder()
                        .serverDateTime(FORMATTER.format(LocalDateTime.now()))
                        .build())
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message, Object errors) {
        return ApiResponse.<T>builder()
                .status(Status.builder()
                        .code(code)
                        .message(message)
                        .errors(errors)
                        .build())
                .data(null)
                .meta(Meta.builder()
                        .serverDateTime(FORMATTER.format(LocalDateTime.now()))
                        .build())
                .build();
    }
}
