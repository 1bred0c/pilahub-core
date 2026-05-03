package fpt.edu.sep490.pilahub.dto.request.tickettype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update an existing ticket type")
public record UpdateTicketTypeRequest(
        @Schema(description = "Ticket type name", example = "PAYMENT_ISSUE") @Size(max = 100, message = "Ticket type name must not exceed 100 characters") String name,

        @Schema(description = "Ticket type description", example = "Issues related to payment processing") @Size(max = 500, message = "Description must not exceed 500 characters") String description) {
}
