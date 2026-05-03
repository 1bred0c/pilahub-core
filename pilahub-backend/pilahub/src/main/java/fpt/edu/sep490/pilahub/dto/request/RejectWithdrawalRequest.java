package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for admin to reject a withdrawal")
public record RejectWithdrawalRequest(
        @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
        @Schema(description = "Admin's rejection reason", example = "Invalid bank account")
        String adminNote
) {
}
