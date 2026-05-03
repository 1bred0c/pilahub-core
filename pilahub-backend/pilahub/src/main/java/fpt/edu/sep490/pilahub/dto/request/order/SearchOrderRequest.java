package fpt.edu.sep490.pilahub.dto.request.order;

import fpt.edu.sep490.pilahub.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to search orders with filters and pagination")
public record SearchOrderRequest(
        @Schema(description = "Search keyword (order number, recipient name, phone, address)", example = "John")
        String keyword,

        @Schema(description = "Filter by order status", example = "PENDING")
        OrderStatus status,

        @Schema(description = "Filter by payment status", example = "true")
        Boolean isPaid,

        @Schema(description = "Filter by account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Start date for date range filter", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "End date for date range filter", example = "2026-12-31T23:59:59Z")
        Instant endDate,

        @Schema(description = "Minimum order amount", example = "100000.00")
        BigDecimal minAmount,

        @Schema(description = "Maximum order amount", example = "1000000.00")
        BigDecimal maxAmount,

        @Schema(description = "Payment method", example = "VNPAY")
        String paymentMethod,

        @Schema(description = "Page number (0-based)", example = "0")
        @Min(value = 0, message = "Page number must be 0 or greater")
        Integer page,

        @Schema(description = "Page size", example = "10")
        @Min(value = 1, message = "Page size must be at least 1")
        @Max(value = 100, message = "Page size must not exceed 100")
        Integer size,

        @Schema(description = "Sort field", example = "createdAt")
        String sortBy,

        @Schema(description = "Sort direction (ASC or DESC)", example = "DESC")
        String sortDirection
) {
    /**
     * Get pageable with safe defaults
     */
    public Pageable getPageable() {
        int pageNumber = page != null ? Math.max(0, page) : 0;
        int pageSize = size != null ? Math.min(Math.max(1, size), 100) : 10;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortField));
    }

    /**
     * Check if any filter is applied
     */
    public boolean hasFilters() {
        return (keyword != null && !keyword.isBlank()) ||
                accountId != null ||
                status != null ||
                isPaid != null ||
                (paymentMethod != null && !paymentMethod.isBlank()) ||
                minAmount != null ||
                maxAmount != null ||
                startDate != null ||
                endDate != null;
    }

    /**
     * Check if date range filter is applied
     */
    public boolean hasDateRangeFilter() {
        return startDate != null || endDate != null;
    }

    /**
     * Check if amount range filter is applied
     */
    public boolean hasAmountRangeFilter() {
        return minAmount != null || maxAmount != null;
    }
}