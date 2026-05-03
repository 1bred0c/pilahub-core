package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ReportReasonDto;
import fpt.edu.sep490.pilahub.dto.request.reportreason.CreateReportReasonRequest;
import fpt.edu.sep490.pilahub.dto.request.reportreason.UpdateReportReasonRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.ReportReasonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/report-reasons")
@RequiredArgsConstructor
@Tag(name = "Report Reason", description = "Report reason management endpoints")
public class ReportReasonController {

    private final ReportReasonService reportReasonService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create report reason", description = "Create a new report reason")
    @ApiResponse(responseCode = "201", description = "Report reason created successfully")
    public ResponseEntity<APIResponse<ReportReasonDto>> createReportReason(@Valid @RequestBody CreateReportReasonRequest request) {
        ReportReasonDto reportReason = reportReasonService.createReportReason(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Report reason created successfully", reportReason));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report reason by ID", description = "Retrieve a specific report reason by its ID")
    public ResponseEntity<APIResponse<ReportReasonDto>> getById(@PathVariable("id") UUID reportReasonId) {
        ReportReasonDto reportReason = reportReasonService.getById(reportReasonId);
        return ResponseEntity.ok(APIResponse.success("Report reason retrieved successfully", reportReason));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get report reason by code", description = "Retrieve a specific report reason by its code")
    public ResponseEntity<APIResponse<ReportReasonDto>> getByCode(@PathVariable String code) {
        ReportReasonDto reportReason = reportReasonService.getByCode(code);
        return ResponseEntity.ok(APIResponse.success("Report reason retrieved successfully", reportReason));
    }

    @GetMapping
    @Operation(summary = "Get all report reasons", description = "Retrieve all report reasons")
    public ResponseEntity<APIResponse<List<ReportReasonDto>>> getAll() {
        List<ReportReasonDto> reportReasons = reportReasonService.getAll();
        return ResponseEntity.ok(APIResponse.success("Report reasons retrieved successfully", reportReasons));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active report reasons", description = "Retrieve all active report reasons")
    public ResponseEntity<APIResponse<List<ReportReasonDto>>> getAllActive() {
        List<ReportReasonDto> reportReasons = reportReasonService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active report reasons retrieved successfully", reportReasons));
    }

    @GetMapping("/search")
    @Operation(summary = "Search report reasons by name", description = "Search report reasons by name")
    public ResponseEntity<APIResponse<List<ReportReasonDto>>> searchByName(@RequestParam("name") String name) {
        List<ReportReasonDto> reportReasons = reportReasonService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", reportReasons));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update report reason", description = "Update a report reason")
    public ResponseEntity<APIResponse<ReportReasonDto>> updateReportReason(
            @PathVariable("id") UUID reportReasonId,
            @Valid @RequestBody UpdateReportReasonRequest request) {
        ReportReasonDto reportReason = reportReasonService.updateReportReason(reportReasonId, request);
        return ResponseEntity.ok(APIResponse.success("Report reason updated successfully", reportReason));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate report reason", description = "Deactivate a report reason")
    public ResponseEntity<APIResponse<Void>> deactivateReportReason(@PathVariable("id") UUID reportReasonId) {
        reportReasonService.deactivateReportReason(reportReasonId);
        return ResponseEntity.ok(APIResponse.success("Report reason deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete report reason", description = "Delete a report reason")
    public ResponseEntity<APIResponse<Void>> deleteReportReason(@PathVariable("id") UUID reportReasonId) {
        reportReasonService.deleteReportReason(reportReasonId);
        return ResponseEntity.ok(APIResponse.success("Report reason deleted successfully", null));
    }
}

