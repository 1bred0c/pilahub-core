package fpt.edu.sep490.pilahub.dto.request.supplement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new supplement")
public record CreateSupplementRequest(
        @Schema(description = "Supplement name", example = "Whey Protein", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Supplement name must not be blank")
        @Size(max = 255, message = "Supplement name must not exceed 255 characters")
        String name,

        @Schema(description = "Supplement description", example = "High-quality whey protein for muscle recovery")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Schema(description = "Brand name", example = "Optimum Nutrition")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @Schema(description = "Supplement form", example = "Powder")
        @Size(max = 100, message = "Form must not exceed 100 characters")
        String form,

        @Schema(description = "Usage instructions", example = "Mix with water or milk, consume after workout")
        @Size(max = 500, message = "Usage instructions must not exceed 500 characters")
        String usageInstructions,

        @Schema(description = "Benefits", example = "Supports muscle growth and recovery")
        @Size(max = 1000, message = "Benefits must not exceed 1000 characters")
        String benefits,

        @Schema(description = "Possible side effects", example = "May cause bloating in some individuals")
        @Size(max = 500, message = "Side effects must not exceed 500 characters")
        String sideEffects,

        @Schema(description = "Contraindications", example = "Not suitable for lactose intolerant individuals")
        @Size(max = 500, message = "Contraindications must not exceed 500 characters")
        String contraindications,

        @Schema(description = "Warnings", example = "Consult physician before use if pregnant or nursing")
        @Size(max = 500, message = "Warnings must not exceed 500 characters")
        String warnings,

        @Schema(description = "Image URL of the supplement", example = "https://example.com/supplements/whey-protein.jpg")
        @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
        String imageUrl
) {
}
