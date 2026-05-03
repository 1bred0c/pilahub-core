package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.service.OrderReportService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/shop-reports")
@RequiredArgsConstructor
@Tag(name = "Shop Report", description = "Vendor shop analytics and reporting endpoints")
public class ShopReportController {

    private final OrderReportService orderReportService;
    private final SecurityUtil securityUtil;

    // ========== REVENUE REPORTS ==========

    @GetMapping("/revenue/total")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop total revenue", description = "Get total revenue for your shop")
    @ApiResponse(responseCode = "200", description = "Shop revenue retrieved successfully")
    public ResponseEntity<APIResponse<BigDecimal>> getShopRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        BigDecimal revenue = orderReportService.getVendorRevenueByDateRange(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Shop revenue retrieved successfully", revenue));
    }

    @GetMapping("/revenue/daily")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop daily revenue", description = "Get daily revenue breakdown for your shop")
    @ApiResponse(responseCode = "200", description = "Shop daily revenue retrieved successfully")
    public ResponseEntity<APIResponse<Map<String, BigDecimal>>> getShopDailyRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Map<String, BigDecimal> revenue = orderReportService.getVendorDailyRevenue(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Shop daily revenue retrieved successfully", revenue));
    }

    @GetMapping("/revenue/monthly")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop monthly revenue", description = "Get monthly revenue breakdown for your shop")
    @ApiResponse(responseCode = "200", description = "Shop monthly revenue retrieved successfully")
    public ResponseEntity<APIResponse<Map<String, BigDecimal>>> getShopMonthlyRevenue(
            @RequestParam("year") @Parameter(description = "Year (e.g., 2026)") Integer year) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Map<String, BigDecimal> revenue = orderReportService.getVendorMonthlyRevenue(vendorId, year);
        return ResponseEntity.ok(APIResponse.success("Shop monthly revenue retrieved successfully", revenue));
    }

    // ========== ORDER STATISTICS ==========

    @GetMapping("/statistics/orders-count")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop orders count", description = "Get total count of orders for your shop")
    @ApiResponse(responseCode = "200", description = "Shop order count retrieved successfully")
    public ResponseEntity<APIResponse<Long>> getShopOrdersCount(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Long count = orderReportService.getVendorOrdersCountByDateRange(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Shop order count retrieved successfully", count));
    }

    @GetMapping("/statistics/status-distribution")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop order status distribution", description = "Get order count by status for your shop")
    @ApiResponse(responseCode = "200", description = "Shop status distribution retrieved successfully")
    public ResponseEntity<APIResponse<Map<OrderStatus, Long>>> getShopOrderStatusDistribution(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Map<OrderStatus, Long> distribution = orderReportService.getVendorOrderStatusDistribution(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Shop status distribution retrieved successfully", distribution));
    }

    @GetMapping("/statistics/average-order-value")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop average order value", description = "Get average order value for your shop")
    @ApiResponse(responseCode = "200", description = "Average order value retrieved successfully")
    public ResponseEntity<APIResponse<BigDecimal>> getShopAverageOrderValue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        BigDecimal avgValue = orderReportService.getVendorAverageOrderValue(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Average order value retrieved successfully", avgValue));
    }

    // ========== PRODUCT ANALYTICS ==========

    @GetMapping("/products/top-products")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop top products", description = "Get most ordered products in your shop")
    @ApiResponse(responseCode = "200", description = "Shop top products retrieved successfully")
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getShopMostOrderedProducts(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", defaultValue = "10") @Parameter(description = "Number of results") Integer limit) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Map<String, Object>> products = orderReportService.getVendorMostOrderedProducts(vendorId, start, end,
                limit);
        return ResponseEntity.ok(APIResponse.success("Shop top products retrieved successfully", products));
    }

    @GetMapping("/products/performance")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop product performance", description = "Get detailed product performance metrics for your shop")
    @ApiResponse(responseCode = "200", description = "Product performance retrieved successfully")
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getShopProductPerformance(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Map<String, Object>> performance = orderReportService.getVendorProductPerformance(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Product performance retrieved successfully", performance));
    }

    @GetMapping("/products/top-by-revenue")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop top products by revenue", description = "Get top revenue-generating products in your shop")
    @ApiResponse(responseCode = "200", description = "Top products by revenue retrieved successfully")
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getShopTopProductsByRevenue(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "limit", defaultValue = "10") @Parameter(description = "Number of results") Integer limit) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        List<Map<String, Object>> products = orderReportService.getVendorTopProductsByRevenue(vendorId, start, end,
                limit);
        return ResponseEntity.ok(APIResponse.success("Top products by revenue retrieved successfully", products));
    }

    // ========== EXPORT REPORTS ==========

    @GetMapping("/export/sales-report-pdf")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Export shop sales report PDF", description = "Generate PDF sales report for your shop")
    @ApiResponse(responseCode = "200", description = "PDF generated successfully")
    public ResponseEntity<byte[]> exportShopSalesReportPDF(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        byte[] pdf = orderReportService.generateVendorSalesReportPDF(vendorId, start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment",
                "shop-sales-report-" + startDate + "-to-" + endDate + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/export/sales-report-excel")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Export shop sales report Excel", description = "Generate Excel sales report for your shop")
    @ApiResponse(responseCode = "200", description = "Excel generated successfully")
    public ResponseEntity<byte[]> exportShopSalesReportExcel(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        byte[] excel = orderReportService.generateVendorSalesReportExcel(vendorId, start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment",
                "shop-sales-report-" + startDate + "-to-" + endDate + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(excel);
    }

    @GetMapping("/export/orders-csv")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Export shop orders CSV", description = "Export your shop orders to CSV")
    @ApiResponse(responseCode = "200", description = "CSV generated successfully")
    public ResponseEntity<byte[]> exportShopOrdersToCSV(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        byte[] csv = orderReportService.exportVendorOrdersToCSV(vendorId, start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "shop-orders-" + startDate + "-to-" + endDate + ".csv");

        return ResponseEntity.ok().headers(headers).body(csv);
    }

    // ========== DASHBOARD METRICS ==========

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Get shop dashboard metrics", description = "Get comprehensive dashboard metrics for your shop")
    @ApiResponse(responseCode = "200", description = "Shop dashboard metrics retrieved successfully")
    public ResponseEntity<APIResponse<Map<String, Object>>> getShopDashboardMetrics(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UUID vendorId = securityUtil.getCurrentUserId();
        Instant start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Map<String, Object> metrics = orderReportService.getVendorDashboardMetrics(vendorId, start, end);
        return ResponseEntity.ok(APIResponse.success("Shop dashboard metrics retrieved successfully", metrics));
    }

    @GetMapping("/dashboard/performance-comparison")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get shop performance comparison", description = "Compare your shop performance between two periods")
    @ApiResponse(responseCode = "200", description = "Performance comparison retrieved successfully")
    public ResponseEntity<APIResponse<Map<String, Object>>> getShopPerformanceComparison(
            @RequestParam("currentStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentStartDate,
            @RequestParam("currentEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentEndDate,
            @RequestParam("previousStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousStartDate,
            @RequestParam("previousEndDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate previousEndDate) {
        UUID vendorId = securityUtil.getCurrentUserId();

        Instant currentStart = currentStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant currentEnd = currentEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();
        Instant previousStart = previousStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant previousEnd = previousEndDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

        Map<String, Object> comparison = orderReportService.getVendorPerformanceComparison(
                vendorId, currentStart, currentEnd, previousStart, previousEnd);
        return ResponseEntity.ok(APIResponse.success("Performance comparison retrieved successfully", comparison));
    }
}
