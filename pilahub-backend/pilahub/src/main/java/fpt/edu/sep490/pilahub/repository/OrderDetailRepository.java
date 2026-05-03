package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {

    List<OrderDetail> findByOrder_OrderId(UUID orderId);

    List<OrderDetail> findByProduct_ProductId(UUID productId);

    List<OrderDetail> findByOrder_Account_AccountId(UUID accountId);

    List<OrderDetail> findByStatusAndShipment_ReturnDeadlineLessThan(OrderDetailStatus status, Instant deadline);

    long deleteByOrder_OrderId(UUID orderId);
}
