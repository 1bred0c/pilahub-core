package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.OrderDetailDto;
import fpt.edu.sep490.pilahub.dto.request.order.RequestOrderDetailReturnRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.service.OrderDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/order-details")
@RequiredArgsConstructor
@Tag(name = "Order Detail", description = "Order detail management endpoints")
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    // ========== CORE CRUD ==========

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get order detail by ID", description = "Retrieve an order detail by its ID")
    @ApiResponse(responseCode = "200", description = "Order detail retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<OrderDetailDto>> getOrderDetailById(
            @PathVariable @Parameter(description = "Order detail ID") UUID id) {
        OrderDetailDto orderDetail = orderDetailService.getOrderDetailById(id);
        return ResponseEntity.ok(APIResponse.success("Order detail retrieved successfully", orderDetail));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get order details by order ID", description = "Retrieve all order details for a specific order")
    @ApiResponse(responseCode = "200", description = "Order details retrieved successfully")
    public ResponseEntity<APIResponse<List<OrderDetailDto>>> getOrderDetailsByOrderId(
            @PathVariable @Parameter(description = "Order ID") UUID orderId) {
        List<OrderDetailDto> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);
        return ResponseEntity.ok(APIResponse.success("Order details retrieved successfully", orderDetails));
    }

    @GetMapping("/order/{orderId}/product/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get order detail by order and product", description = "Retrieve a specific order detail for an order and product")
    @ApiResponse(responseCode = "200", description = "Order detail retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<OrderDetailDto>> getOrderDetailByOrderIdAndProductId(
            @PathVariable @Parameter(description = "Order ID") UUID orderId,
            @PathVariable @Parameter(description = "Product ID") UUID productId) {
        OrderDetailDto orderDetail = orderDetailService.getOrderDetailByOrderAndProduct(orderId, productId);
        return ResponseEntity.ok(APIResponse.success("Order detail retrieved successfully", orderDetail));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Delete order detail", description = "Delete an order detail by ID")
    @ApiResponse(responseCode = "200", description = "Order detail deleted successfully")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<Void>> deleteOrderDetail(
            @PathVariable @Parameter(description = "Order detail ID") UUID id) {
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok(APIResponse.success("Order detail deleted successfully", null));
    }

    // ========== ORDER DETAIL OPERATIONS ==========

    @PutMapping("/{id}/quantity")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Update order detail quantity", description = "Update the quantity of an order detail and recalculate subtotal")
    @ApiResponse(responseCode = "200", description = "Quantity updated successfully")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    @ApiResponse(responseCode = "400", description = "Invalid quantity or order cannot be modified")
    public ResponseEntity<APIResponse<OrderDetailDto>> updateOrderDetailQuantity(
            @PathVariable @Parameter(description = "Order detail ID") UUID id,
            @RequestParam @Parameter(description = "New quantity (must be > 0)") Integer newQuantity) {
        OrderDetailDto orderDetail = orderDetailService.updateOrderDetailQuantity(id, newQuantity);
        return ResponseEntity.ok(APIResponse.success("Order detail quantity updated successfully", orderDetail));
    }

    @PutMapping("/{id}/discount")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Update order detail discount", description = "Update the discount amount for an order detail")
    @ApiResponse(responseCode = "200", description = "Discount updated successfully")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    @ApiResponse(responseCode = "400", description = "Invalid discount amount")
    public ResponseEntity<APIResponse<OrderDetailDto>> updateOrderDetailDiscount(
            @PathVariable @Parameter(description = "Order detail ID") UUID id,
            @RequestParam @Parameter(description = "New discount amount (must be >= 0)") BigDecimal newDiscountAmount) {
        OrderDetailDto orderDetail = orderDetailService.updateOrderDetailDiscount(id, newDiscountAmount);
        return ResponseEntity.ok(APIResponse.success("Order detail discount updated successfully", orderDetail));
    }

    @PostMapping("/duplicate")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Duplicate order details", description = "Duplicate all order details from one order to another (for re-ordering)")
    @ApiResponse(responseCode = "200", description = "Order details duplicated successfully")
    @ApiResponse(responseCode = "404", description = "Source or target order not found")
    @ApiResponse(responseCode = "400", description = "Target order already has items")
    public ResponseEntity<APIResponse<List<OrderDetailDto>>> duplicateOrderDetails(
            @RequestParam @Parameter(description = "Source order ID") UUID fromOrderId,
            @RequestParam @Parameter(description = "Target order ID") UUID toOrderId) {
        List<OrderDetailDto> duplicatedDetails = orderDetailService.duplicateOrderDetails(fromOrderId, toOrderId);
        return ResponseEntity.ok(APIResponse.success("Order details duplicated successfully", duplicatedDetails));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Request order detail return", description = "Request a return for a delivered order detail within the return deadline")
    @ApiResponse(responseCode = "200", description = "Return requested successfully")
    @ApiResponse(responseCode = "400", description = "Order detail is not eligible for return")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<OrderDetailDto>> requestOrderDetailReturn(
            @PathVariable @Parameter(description = "Order detail ID") UUID id,
            @Valid @RequestBody RequestOrderDetailReturnRequest request) {
        OrderDetailDto orderDetail = orderDetailService.requestReturn(id, request.reason());
        return ResponseEntity.ok(APIResponse.success("Return requested successfully", orderDetail));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Process order detail refund", description = "Process refund for a returned or cancelled order detail")
    @ApiResponse(responseCode = "200", description = "Refund processed successfully")
    @ApiResponse(responseCode = "400", description = "Order detail is not eligible for refund")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<OrderDetailDto>> processOrderDetailRefund(
            @PathVariable @Parameter(description = "Order detail ID") UUID id) {
        OrderDetailDto orderDetail = orderDetailService.processRefund(id);
        return ResponseEntity.ok(APIResponse.success("Refund processed successfully", orderDetail));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Confirm order detail delivered",
            description = "Trainee/customer confirms a delivered order detail. Status defaults to COMPLETED")
    @ApiResponse(responseCode = "200", description = "Order detail confirmed successfully")
    @ApiResponse(responseCode = "400", description = "Only COMPLETED status is allowed for this endpoint")
    @ApiResponse(responseCode = "404", description = "Order detail not found")
    public ResponseEntity<APIResponse<OrderDetailDto>> confirmOrderDetailDelivered(
            @PathVariable @Parameter(description = "Order detail ID") UUID id,
            @RequestParam(defaultValue = "COMPLETED")
            @Parameter(description = "Confirmation status, defaults to COMPLETED") OrderDetailStatus status) {
        if (status != OrderDetailStatus.COMPLETED) {
            throw new IllegalArgumentException("Only COMPLETED status is supported for confirmation");
        }

        OrderDetailDto orderDetail = orderDetailService.updateOrderDetailStatus(id, status);
        return ResponseEntity.ok(APIResponse.success("Order detail confirmed successfully", orderDetail));
    }
}
