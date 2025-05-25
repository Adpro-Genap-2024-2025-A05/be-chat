package id.ac.ui.cs.advprog.bechat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
public class BaseResponseDTO<T> {
    private int status;
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Jakarta")
    private Date timestamp;

    private T data;

    public static <T> BaseResponseDTO<T> success(int status, String message, T data) {
        return BaseResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .timestamp(new Date())
                .data(data)
                .build();
    }

    public static <T> BaseResponseDTO<T> error(int status, String message) {
        return BaseResponseDTO.<T>builder()
                .status(status)
                .message(message)
                .timestamp(new Date())
                .data(null)
                .build();
    }
}
