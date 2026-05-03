package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.OrderDto;
import fpt.edu.sep490.pilahub.dto.request.order.*;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.service.OrderService;
import fpt.edu.sep490.pilahub.service.VendorPayoutService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;
    private final VendorPayoutService vendorPayoutService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Place order", description = "Place an order for the current user. Items from multiple vendors are automatically split into separate vendor orders.")
    @ApiResponse(responseCode = "201", description = "Orders created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<OrderDto>>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        List<OrderDto> orders = orderService.createOrder(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Orders placed successfully", orders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> getOrderById(@PathVariable("id") UUID orderId) {
        OrderDto order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(APIResponse.success("Order retrieved successfully", order));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieve an order using its order number")
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> getOrderByOrderNumber(
            @PathVariable("orderNumber") @Parameter(description = "Order number") String orderNumber) {
        OrderDto order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(APIResponse.success("Order retrieved successfully", order));
    }

    @GetMapping("/tracking/{orderNumber}")
    @Operation(summary = "Track order", description = "Track order status by order number (public endpoint)")
    @ApiResponse(responseCode = "200", description = "Order tracking information retrieved")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> trackOrder(
            @PathVariable("orderNumber") @Parameter(description = "Order number") String orderNumber) {
        OrderDto order = orderService.getOrderTracking(orderNumber);
        return ResponseEntity.ok(APIResponse.success("Order tracking information retrieved", order));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all orders", description = "Admin: get all orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<OrderDto>>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Get my orders", description = "Get all orders for the current user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<APIResponse<List<OrderDto>>> getMyOrders() {
        UUID accountId = securityUtil.getCurrentUserId();
        List<OrderDto> orders = orderService.getOrdersByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/my-orders/unpaid")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Get my unpaid non-COD orders", description = "Get current user's orders where paymentMethod != COD and paid = false")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<APIResponse<List<OrderDto>>> getMyUnpaidNonCodOrders() {
        UUID accountId = securityUtil.getCurrentUserId();
        List<OrderDto> orders = orderService.getUnpaidNonCodOrdersByAccountId(accountId);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/my-orders/status/{status}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Get my orders by status", description = "Get user orders filtered by status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<APIResponse<List<OrderDto>>> getMyOrdersByStatus(
            @PathVariable("status") @Parameter(description = "Order status") OrderStatus status) {
        UUID accountId = securityUtil.getCurrentUserId();
        List<OrderDto> orders = orderService.getOrdersByAccountIdAndStatus(accountId, status);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/vendor/my-orders")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get my responsible orders", description = "Get all orders that the current vendor is responsible for")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<APIResponse<List<OrderDto>>> getMyResponsibleOrders() {
        UUID vendorId = securityUtil.getCurrentUserId();
        List<OrderDto> orders = orderService.getOrdersByVendorId(vendorId);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @GetMapping("/admin/vendors/{vendorId}/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get orders by vendor (admin)", description = "Admin: get all orders that a specific vendor is responsible for")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Vendor not found")
    public ResponseEntity<APIResponse<List<OrderDto>>> getOrdersByVendorForAdmin(
            @PathVariable("vendorId") @Parameter(description = "Vendor ID") UUID vendorId) {
        List<OrderDto> orders = orderService.getOrdersByVendorId(vendorId);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Search orders", description = "Search orders with filters and pagination")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<APIResponse<Page<OrderDto>>> searchOrders(
            @Valid @RequestBody SearchOrderRequest searchRequest) {
        Page<OrderDto> orders = orderService.searchOrders(searchRequest);
        return ResponseEntity.ok(APIResponse.success("Orders retrieved successfully", orders));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete order", description = "Delete an order (admin only)")
    @ApiResponse(responseCode = "200", description = "Order deleted successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<Void>> deleteOrder(@PathVariable("id") UUID orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok(APIResponse.success("Order deleted successfully", null));
    }

    // ========== ORDER STATUS MANAGEMENT ==========

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    @ApiResponse(responseCode = "200", description = "Order status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> updateOrderStatus(
            @PathVariable("id") UUID orderId,
            @RequestParam("status") @Parameter(description = "New order status") OrderStatus status) {
        OrderDto order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(APIResponse.success("Order status updated successfully", order));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Cancel order", description = "Cancel an order with a reason")
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Order cannot be cancelled")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> cancelOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        OrderDto order = orderService.cancelOrder(orderId, request);
        return ResponseEntity.ok(APIResponse.success("Order cancelled successfully", order));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Return order", description = "Return an order with a reason")
    @ApiResponse(responseCode = "200", description = "Order returned successfully")
    @ApiResponse(responseCode = "400", description = "Order cannot be returned")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> returnOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody RequestOrderReturnRequest request) {
        OrderDto order = orderService.returnOrder(orderId, request.reason());
        return ResponseEntity.ok(APIResponse.success("Order returned successfully", order));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Complete order (trainee)", description = "Mark a delivered order as completed by the current trainee/user")
    @ApiResponse(responseCode = "200", description = "Order completed successfully")
    @ApiResponse(responseCode = "400", description = "Order cannot be completed")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> completeOrderForTrainee(
            @PathVariable("id") UUID orderId) {
        OrderDto order = orderService.completeOrderForTrainee(orderId);
        return ResponseEntity.ok(APIResponse.success("Order completed successfully", order));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    @Operation(summary = "Process order refund", description = "Process refund for a returned or cancelled order")
    @ApiResponse(responseCode = "200", description = "Order refunded successfully")
    @ApiResponse(responseCode = "400", description = "Order is not eligible for refund")
    @ApiResponse(responseCode = "404", description = "Order not found")
    public ResponseEntity<APIResponse<OrderDto>> processOrderRefund(
            @PathVariable("id") UUID orderId) {
        OrderDto order = orderService.processRefund(orderId);
        return ResponseEntity.ok(APIResponse.success("Order refunded successfully", order));
    }

    // ========== ORDER ITEMS MANAGEMENT ==========

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Add item to order", description = "Add a new item to a pending order")
    @ApiResponse(responseCode = "200", description = "Item added successfully")
    @ApiResponse(responseCode = "400", description = "Cannot modify non-pending order")
    @ApiResponse(responseCode = "404", description = "Order or product not found")
    public ResponseEntity<APIResponse<OrderDto>> addItemToOrder(
            @PathVariable("id") UUID orderId,
            @Valid @RequestBody OrderItemRequest itemRequest) {
        OrderDto order = orderService.addItemToOrder(orderId, itemRequest);
        return ResponseEntity.ok(APIResponse.success("Item added to order successfully", order));
    }

    @DeleteMapping("/{id}/items/{orderDetailId}")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Remove item from order", description = "Remove an item from a pending order")
    @ApiResponse(responseCode = "200", description = "Item removed successfully")
    @ApiResponse(responseCode = "400", description = "Cannot modify non-pending order or last item")
    @ApiResponse(responseCode = "404", description = "Order or order detail not found")
    public ResponseEntity<APIResponse<OrderDto>> removeItemFromOrder(
            @PathVariable("id") UUID orderId,
            @PathVariable("orderDetailId") UUID orderDetailId) {
        OrderDto order = orderService.removeItemFromOrder(orderId, orderDetailId);
        return ResponseEntity.ok(APIResponse.success("Item removed from order successfully", order));
    }

    @PutMapping("/{id}/items/{orderDetailId}/quantity")
    @PreAuthorize("hasAnyRole('USER', 'TRAINEE')")
    @Operation(summary = "Update item quantity", description = "Update quantity of an item in a pending order")
    @ApiResponse(responseCode = "200", description = "Item quantity updated successfully")
    @ApiResponse(responseCode = "400", description = "Cannot modify non-pending order")
    @ApiResponse(responseCode = "404", description = "Order or order detail not found")
    public ResponseEntity<APIResponse<OrderDto>> updateOrderItemQuantity(
            @PathVariable("id") UUID orderId,
            @PathVariable("orderDetailId") UUID orderDetailId,
            @RequestParam("quantity") @Parameter(description = "New quantity") Integer quantity) {
        OrderDto order = orderService.updateOrderItemQuantity(orderId, orderDetailId, quantity);
        return ResponseEntity.ok(APIResponse.success("Item quantity updated successfully", order));
    }

    // ========== VENDOR PAYOUTS ==========

    @PostMapping("/{id}/vendor-payout")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Payout vendor for order", description = "Admin-triggered payout for a specific order. " +
            "Checks paidOut status only and ignores returnDeadline/payoutReleaseDate.")
    @ApiResponse(responseCode = "200", description = "Vendor payout processed successfully")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "400", description = "Order cannot be paid out")
    public ResponseEntity<APIResponse<Void>> payoutOrderForVendor(@PathVariable("id") UUID orderId) {
        vendorPayoutService.payoutOrderForVendor(orderId);
        return ResponseEntity.ok(APIResponse.success("Vendor payout processed successfully", null));
    }
}
