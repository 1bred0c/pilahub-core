package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PersonalInjuryDto;
import fpt.edu.sep490.pilahub.dto.request.injury.CreatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.dto.request.injury.UpdatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.service.PersonalInjuryService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal-injuries")
@RequiredArgsConstructor
@Tag(name = "Personal Injury", description = "Manage personal injury records for trainees")
public class PersonalInjuryController {

    private final PersonalInjuryService personalInjuryService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Create personal injury", description = "Add a new injury to trainee's personal injury record")
    @ApiResponse(responseCode = "201", description = "Personal injury created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Injury not found in library")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PersonalInjuryDto>> createPersonalInjury(
            @Valid @RequestBody CreatePersonalInjuryRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        PersonalInjuryDto personalInjury = personalInjuryService.createPersonalInjury(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal injury created successfully", personalInjury));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get personal injury by ID", description = "Retrieve a specific personal injury record by its ID")
    @ApiResponse(responseCode = "200", description = "Personal injury found")
    @ApiResponse(responseCode = "404", description = "Personal injury not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PersonalInjuryDto>> getPersonalInjuryById(@PathVariable UUID id) {
        PersonalInjuryDto personalInjury = personalInjuryService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Personal injury retrieved successfully", personalInjury));
    }

    @GetMapping("/my-injuries")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my injuries", description = "Retrieve all personal injuries for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Personal injuries retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<PersonalInjuryDto>>> getMyInjuries() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<PersonalInjuryDto> injuries = personalInjuryService.getMyInjuries(traineeId);
        return ResponseEntity.ok(APIResponse.success("Personal injuries retrieved successfully", injuries));
    }

    @GetMapping("/my-injuries/active")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my active injuries", description = "Retrieve all active injuries for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Active injuries retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<PersonalInjuryDto>>> getMyActiveInjuries() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<PersonalInjuryDto> injuries = personalInjuryService.getMyInjuriesByStatus(traineeId, InjuryStatus.ACTIVE);
        return ResponseEntity.ok(APIResponse.success("Active injuries retrieved successfully", injuries));
    }

    @GetMapping("/my-injuries/recovered")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my recovered injuries", description = "Retrieve all recovered injuries for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Recovered injuries retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<PersonalInjuryDto>>> getMyRecoveredInjuries() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<PersonalInjuryDto> injuries = personalInjuryService.getMyInjuriesByStatus(traineeId, InjuryStatus.RECOVERED);
        return ResponseEntity.ok(APIResponse.success("Recovered injuries retrieved successfully", injuries));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Update personal injury", description = "Update status or notes of a personal injury record")
    @ApiResponse(responseCode = "200", description = "Personal injury updated successfully")
    @ApiResponse(responseCode = "404", description = "Personal injury not found")
    @ApiResponse(responseCode = "400", description = "Invalid input or not the owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PersonalInjuryDto>> updatePersonalInjury(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonalInjuryRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        PersonalInjuryDto personalInjury = personalInjuryService.updatePersonalInjury(id, traineeId, request);
        return ResponseEntity.ok(APIResponse.success("Personal injury updated successfully", personalInjury));
    }

    @PatchMapping("/{id}/recover")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Mark injury as recovered", description = "Mark a personal injury as recovered")
    @ApiResponse(responseCode = "200", description = "Injury marked as recovered")
    @ApiResponse(responseCode = "404", description = "Personal injury not found")
    @ApiResponse(responseCode = "400", description = "Not the owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<PersonalInjuryDto>> markAsRecovered(@PathVariable UUID id) {
        UUID traineeId = securityUtil.getCurrentUserId();
        PersonalInjuryDto personalInjury = personalInjuryService.markAsRecovered(id, traineeId);
        return ResponseEntity.ok(APIResponse.success("Injury marked as recovered", personalInjury));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete personal injury", description = "Permanently delete a personal injury record")
    @ApiResponse(responseCode = "200", description = "Personal injury deleted successfully")
    @ApiResponse(responseCode = "404", description = "Personal injury not found")
    @ApiResponse(responseCode = "400", description = "Not the owner")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deletePersonalInjury(@PathVariable UUID id) {
        UUID traineeId = securityUtil.getCurrentUserId();
        personalInjuryService.deletePersonalInjury(id, traineeId);
        return ResponseEntity.ok(APIResponse.success("Personal injury deleted successfully", null));
    }
}
