package bf.gov.anptic.sonagess.cbc.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "Opération effectuée avec succès");
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
