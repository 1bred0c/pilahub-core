package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.pojo.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

        List<Order> findAllByOrderByCreatedAtDesc();

        List<Order> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);

        List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

        List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, Instant createdAt);

        List<Order> findByAccount_AccountIdAndStatusOrderByCreatedAtDesc(UUID accountId, OrderStatus status);

        @Query("SELECT DISTINCT o FROM Order o " +
                        "LEFT JOIN o.shipments s " +
                        "LEFT JOIN o.orderDetails od " +
                        "LEFT JOIN od.product p " +
                        "LEFT JOIN p.vendor v " +
                        "WHERE (s.vendor.vendorId = :vendorId " +
                        "OR v.vendorId = :vendorId) " +
                        "AND NOT (o.status = fpt.edu.sep490.pilahub.enums.OrderStatus.PENDING " +
                        "AND o.paid = false " +
                        "AND (o.paymentMethod IS NULL OR UPPER(o.paymentMethod) <> 'COD')) " +
                        "ORDER BY o.createdAt DESC")
        List<Order> findDistinctByVendorResponsibility(@Param("vendorId") UUID vendorId);

        @Query("SELECT o FROM Order o " +
                        "WHERE o.account.accountId = :accountId " +
                        "AND o.paid = false " +
                        "AND (o.paymentMethod IS NULL OR UPPER(o.paymentMethod) <> 'COD') " +
                        "ORDER BY o.createdAt DESC")
        List<Order> findUnpaidNonCodOrdersByAccountId(@Param("accountId") UUID accountId);

        @Query("SELECT o FROM Order o " +
                        "WHERE o.paid = false " +
                        "AND (o.paymentMethod IS NULL OR UPPER(o.paymentMethod) <> 'COD')")
        List<Order> findAllUnpaidNonCodOrders();

        Optional<Order> findByOrderNumber(String orderNumber);

        List<Order> findByPaidTrue();

        List<Order> findByPaidFalse();

        @Query("SELECT o FROM Order o " +
                        "WHERE o.paidOut = false " +
                        "AND EXISTS (SELECT od FROM OrderDetail od WHERE od.order = o AND od.status = :status) " +
                        "AND NOT EXISTS (SELECT od2 FROM OrderDetail od2 WHERE od2.order = o AND od2.status <> :status) "
                        +
                        "AND EXISTS (SELECT s FROM Shipment s WHERE s.order = o AND s.vendor IS NOT NULL)")
        List<Order> findEligibleForVendorPayoutByOrder(@Param("status") OrderDetailStatus status);

        boolean existsByOrderNumber(String orderNumber);
}
