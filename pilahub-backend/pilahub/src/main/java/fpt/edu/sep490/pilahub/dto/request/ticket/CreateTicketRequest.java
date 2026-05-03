package fpt.edu.sep490.pilahub.dto.request.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new ticket")
public record CreateTicketRequest(
        @Schema(description = "Ticket type ID", example = "123e4567-e89b-12d3-a456-426614174002", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Ticket type ID must not be null") UUID ticketTypeId,

        @Schema(description = "Ticket title", example = "Cannot complete payment", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Title must not be blank") @Size(max = 255, message = "Title must not exceed 255 characters") String title,

        @Schema(description = "Ticket description", example = "I tried to pay but got an error code.", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Description must not be blank") @Size(max = 2000, message = "Description must not exceed 2000 characters") String description) {
}
