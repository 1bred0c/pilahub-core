package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for admin to approve a withdrawal")
public record ApproveWithdrawalRequest(
        @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
        @Schema(description = "Admin's approval note", example = "Approved - Processing transfer")
        String adminNote
) {
}
