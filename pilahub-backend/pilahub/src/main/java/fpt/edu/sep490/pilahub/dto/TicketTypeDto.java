package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Ticket type information")
public record TicketTypeDto(
        @Schema(description = "Unique ticket type identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID ticketTypeId,

        @Schema(description = "Ticket type name", example = "PAYMENT_ISSUE") String name,

        @Schema(description = "Ticket type description", example = "Issues related to payment processing") String description,

        @Schema(description = "Whether the ticket type is active", example = "true") boolean active,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt) {
}
