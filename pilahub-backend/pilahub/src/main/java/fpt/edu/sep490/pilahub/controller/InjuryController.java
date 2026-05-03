package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.InjuryDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.InjuryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/injuries")
@RequiredArgsConstructor
@Tag(name = "Injury Library", description = "Browse common injuries information")
public class InjuryController {

    private final InjuryService injuryService;

    @GetMapping("/{id}")
    @Operation(summary = "Get injury by ID", description = "Retrieve detailed information about a specific injury")
    @ApiResponse(responseCode = "200", description = "Injury found")
    @ApiResponse(responseCode = "404", description = "Injury not found")
    public ResponseEntity<APIResponse<InjuryDto>> getInjuryById(@PathVariable UUID id) {
        InjuryDto injury = injuryService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Injury retrieved successfully", injury));
    }

    @GetMapping
    @Operation(summary = "Get all injuries", description = "Retrieve all injuries from the library")
    @ApiResponse(responseCode = "200", description = "Injuries retrieved successfully")
    public ResponseEntity<APIResponse<List<InjuryDto>>> getAllInjuries() {
        List<InjuryDto> injuries = injuryService.getAllInjuries();
        return ResponseEntity.ok(APIResponse.success("Injuries retrieved successfully", injuries));
    }

    @GetMapping("/search")
    @Operation(summary = "Search injuries by name", description = "Search for injuries by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<APIResponse<List<InjuryDto>>> searchInjuries(
            @RequestParam String name) {
        List<InjuryDto> injuries = injuryService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", injuries));
    }
}
