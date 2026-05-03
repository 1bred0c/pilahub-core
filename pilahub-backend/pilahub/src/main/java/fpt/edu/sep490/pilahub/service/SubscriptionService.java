package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SubscriptionDto;
import fpt.edu.sep490.pilahub.dto.request.SubscribePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpgradePackageRequest;
import fpt.edu.sep490.pilahub.dto.response.UpgradePackageResponse;
import fpt.edu.sep490.pilahub.dto.response.UpgradeablePackageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface SubscriptionService {

    SubscriptionDto subscribePackage(UUID accountId, SubscribePackageRequest request);

    UpgradePackageResponse upgradePackage(UUID accountId, UpgradePackageRequest request);

    SubscriptionDto getSubscriptionById(UUID subscriptionId);

    List<SubscriptionDto> getSubscriptionsByAccountId(UUID accountId);

    SubscriptionDto getActiveSubscriptionByAccountId(UUID accountId);

    Page<SubscriptionDto> getAllSubscriptions(Pageable pageable);

    List<SubscriptionDto> getSubscriptionsByStatus(String status);

    void expireSubscriptions();

    SubscriptionDto cancelSubscription(UUID subscriptionId);

    List<UpgradeablePackageDto> getUpgradeablePackages(UUID accountId);
}
