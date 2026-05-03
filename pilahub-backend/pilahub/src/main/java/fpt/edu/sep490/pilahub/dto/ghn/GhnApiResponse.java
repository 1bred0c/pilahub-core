package fpt.edu.sep490.pilahub.dto.ghn;

/**
 * Generic response wrapper returned by all GHN API endpoints.
 *
 * @param <T> type of the {@code data} payload
 */
public record GhnApiResponse<T>(
        Integer code,
        String message,
        T data
) {}
