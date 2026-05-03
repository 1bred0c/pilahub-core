package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.AdminDashboardOverviewResponse;
import fpt.edu.sep490.pilahub.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin-only dashboard metrics APIs")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard overview (Admin only)", description = "Get counts of trainees/vendors/coaches, today's transaction count, total gross monthly, and coaches sorted by avg rating.")
    @ApiResponse(responseCode = "200", description = "Dashboard overview retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<AdminDashboardOverviewResponse>> getDashboardOverview() {
        AdminDashboardOverviewResponse overview = adminDashboardService.getDashboardOverview();
        return ResponseEntity.ok(APIResponse.success("Dashboard overview retrieved successfully", overview));
    }
}
