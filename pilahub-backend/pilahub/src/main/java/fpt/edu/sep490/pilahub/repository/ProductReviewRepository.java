package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {

    List<ProductReview> findByProduct_ProductId(UUID productId);

    List<ProductReview> findByAccount_AccountId(UUID accountId);

    boolean existsByProduct_ProductIdAndAccount_AccountId(UUID productId, UUID accountId);
}
