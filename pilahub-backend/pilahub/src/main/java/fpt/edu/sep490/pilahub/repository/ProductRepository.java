package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Product;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    List<Product> findByActiveTrue();

    List<Product> findByVendor_VendorId(UUID vendorId);

    List<Product> findByCategory_CategoryId(UUID categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    List<Product> findByNameIn(List<String> names);

    List<Product> findByNameInAndActiveTrue(List<String> names);

    List<Product> findByActiveTrueAndCategory_CategoryId(UUID categoryId);

    List<Product> findByActiveTrueAndVendor_VendorId(UUID vendorId);

    List<Product> findByActiveTrueAndExpiredDateLessThanEqual(Instant now);

    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.productId = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") UUID productId);

    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.product.productId = :productId")
    Integer countReviewsByProductId(@Param("productId") UUID productId);
}
