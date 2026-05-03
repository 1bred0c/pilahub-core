package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SystemConfigDto;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.CreateSystemConfigRequest;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.UpdateSystemConfigRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface SystemConfigService {

    SystemConfigDto create(CreateSystemConfigRequest request);

    SystemConfigDto getById(UUID configId);

    List<SystemConfigDto> getAll();

    SystemConfigDto update(UUID configId, UpdateSystemConfigRequest request);

    void delete(UUID configId);

    SystemConfigDto getCurrentConfig();

    double getDefaultPlatformFeePercentage();

    int getDefaultHoldingDays();

    BigDecimal getHoursPerSlot();

    String getEmailAdmin();

    int getVendorConfirmOrderHours();

    int getProductCreateRequiredExpiryMonths();

    int getActiveProductMinRequiredMonths();
}
