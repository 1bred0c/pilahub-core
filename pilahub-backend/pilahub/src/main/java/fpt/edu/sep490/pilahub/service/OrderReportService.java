package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for order reports and analytics
 */
public interface OrderReportService {

    // ========== REVENUE REPORTS ==========

    /**
     * Get total revenue by date range
     */
    BigDecimal getTotalRevenueByDateRange(Instant startDate, Instant endDate);

    /**
     * Get daily revenue for a date range
     */
    Map<String, BigDecimal> getDailyRevenue(Instant startDate, Instant endDate);

    /**
     * Get monthly revenue for a year
     */
    Map<String, BigDecimal> getMonthlyRevenue(Integer year);

    /**
     * Get total gross revenue by vendor and date range.
     * gross per detail = subtotal - shippingFee
     */
    BigDecimal getVendorRevenueByDateRange(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get total net revenue by vendor and date range.
     * net per detail = gross - discountAmount - platformFee (using vendor's platformFeePercentage)
     */
    BigDecimal getVendorNetRevenueByDateRange(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get daily gross revenue for vendor (gross = subtotal - shippingFee)
     */
    Map<String, BigDecimal> getVendorDailyRevenue(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get monthly revenue for vendor
     */
    Map<String, BigDecimal> getVendorMonthlyRevenue(UUID vendorId, Integer year);

    // ========== ORDER STATISTICS ==========

    /**
     * Get order count by date range
     */
    Long getOrdersCountByDateRange(Instant startDate, Instant endDate);

    /**
     * Get order status distribution by date range
     */
    Map<OrderStatus, Long> getOrderStatusDistribution(Instant startDate, Instant endDate);

    /**
     * Get vendor order count by date range
     */
    Long getVendorOrdersCountByDateRange(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get vendor order status distribution
     */
    Map<OrderStatus, Long> getVendorOrderStatusDistribution(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get vendor average order value
     */
    BigDecimal getVendorAverageOrderValue(UUID vendorId, Instant startDate, Instant endDate);

    // ========== CUSTOMER ANALYTICS ==========

    /**
     * Get top customers by revenue
     */
    List<Map<String, Object>> getTopCustomersByRevenue(Instant startDate, Instant endDate, Integer limit);

    // ========== PRODUCT ANALYTICS ==========

    /**
     * Get most ordered products by date range
     */
    List<Map<String, Object>> getMostOrderedProducts(Instant startDate, Instant endDate, Integer limit);

    /**
     * Get vendor's most ordered products
     */
    List<Map<String, Object>> getVendorMostOrderedProducts(UUID vendorId, Instant startDate, Instant endDate, Integer limit);

    /**
     * Get vendor's product performance (sales, revenue, quantity sold)
     */
    List<Map<String, Object>> getVendorProductPerformance(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Get vendor's best selling products by revenue
     */
    List<Map<String, Object>> getVendorTopProductsByRevenue(UUID vendorId, Instant startDate, Instant endDate, Integer limit);

    // ========== EXPORT REPORTS ==========

    /**
     * Generate comprehensive sales report as PDF
     */
    byte[] generateSalesReportPDF(Instant startDate, Instant endDate);

    /**
     * Generate comprehensive sales report as Excel
     */
    byte[] generateSalesReportExcel(Instant startDate, Instant endDate);

    /**
     * Export orders to CSV
     */
    byte[] exportOrdersToCSV(Instant startDate, Instant endDate);

    /**
     * Generate vendor sales report as PDF
     */
    byte[] generateVendorSalesReportPDF(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Generate vendor sales report as Excel
     */
    byte[] generateVendorSalesReportExcel(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Export vendor orders to CSV
     */
    byte[] exportVendorOrdersToCSV(UUID vendorId, Instant startDate, Instant endDate);

    // ========== DASHBOARD METRICS ==========

    /**
     * Get dashboard summary metrics (includes revenue, order counts, top products, etc.)
     */
    Map<String, Object> getDashboardMetrics(Instant startDate, Instant endDate);

    /**
     * Get vendor dashboard summary metrics (revenue, orders, products, customers)
     */
    Map<String, Object> getVendorDashboardMetrics(UUID vendorId, Instant startDate, Instant endDate);

    /**
     * Compare vendor performance with previous period
     */
    Map<String, Object> getVendorPerformanceComparison(UUID vendorId, Instant currentStart, Instant currentEnd, 
                                                        Instant previousStart, Instant previousEnd);
}
