package fpt.edu.sep490.pilahub.scheduler;

import fpt.edu.sep490.pilahub.pojo.Product;
import fpt.edu.sep490.pilahub.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductExpirationScheduler {

    private final ProductRepository productRepository;

    /**
     * Run every hour to deactivate products that have reached their expiration
     * time.
     * Products without expiredDate are ignored.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void autoDeactivateExpiredProducts() {
        Instant now = Instant.now();
        List<Product> expiredActiveProducts = productRepository.findByActiveTrueAndExpiredDateLessThanEqual(now);

        if (expiredActiveProducts.isEmpty()) {
            log.debug("No expired active products to deactivate");
            return;
        }

        expiredActiveProducts.forEach(product -> product.setActive(false));
        productRepository.saveAll(expiredActiveProducts);

        log.info("Auto-deactivated {} expired product(s)", expiredActiveProducts.size());
    }
}
