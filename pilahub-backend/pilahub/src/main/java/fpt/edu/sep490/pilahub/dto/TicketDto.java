package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ticket information")
public record TicketDto(
        @Schema(description = "Unique ticket identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID ticketId,

        @Schema(description = "Account ID of ticket creator", example = "123e4567-e89b-12d3-a456-426614174001") UUID accountId,

        @Schema(description = "Ticket type ID", example = "123e4567-e89b-12d3-a456-426614174002") UUID ticketTypeId,

        @Schema(description = "Ticket type name", example = "PAYMENT_ISSUE") String ticketTypeName,

        @Schema(description = "Ticket title", example = "Cannot complete payment") String title,

        @Schema(description = "Ticket description", example = "I tried to pay but got an error code.") String description,

        @Schema(description = "Ticket status", example = "PENDING") TicketStatus status,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt) {
}
