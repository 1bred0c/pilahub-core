package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.SystemConfigDto;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.CreateSystemConfigRequest;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.UpdateSystemConfigRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.pojo.SystemConfig;
import fpt.edu.sep490.pilahub.repository.SystemConfigRepository;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String KEY_PLATFORM_FEE_PERCENTAGE = "PLATFORM_FEE_PERCENTAGE";
    private static final String KEY_HOLDING_DAYS = "HOLDING_DAYS";
    private static final String KEY_HOURS_PER_SLOT = "HOURS_PER_SLOT";
    private static final String KEY_EMAIL_ADMIN = "EMAIL_ADMIN";
    private static final String KEY_VENDOR_CONFIRM_ORDER_HOURS = "VENDOR_CONFIRM_ORDER_HOURS";
    private static final String KEY_PRODUCT_CREATE_REQUIRED_EXPIRY_MONTHS = "PRODUCT_CREATE_REQUIRED_EXPIRY_MONTHS";
    private static final String KEY_ACTIVE_PRODUCT_MIN_REQUIRED_MONTHS = "ACTIVE_PRODUCT_MIN_REQUIRED_MONTHS";

    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional
    public SystemConfigDto create(CreateSystemConfigRequest request) {
        String normalizedKey = request.key().trim().toUpperCase();
        if (systemConfigRepository.existsByKey(normalizedKey)) {
            throw new IllegalStateException("System config key already exists: " + normalizedKey);
        }

        SystemConfig entity = SystemConfig.builder()
                .key(normalizedKey)
                .value(request.value().trim())
                .description(request.description())
                .build();

        return toDto(systemConfigRepository.save(entity));
    }

    @Override
    public SystemConfigDto getById(UUID configId) {
        return toDto(findByIdOrThrow(configId));
    }

    @Override
    public List<SystemConfigDto> getAll() {
        return systemConfigRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SystemConfigDto update(UUID configId, UpdateSystemConfigRequest request) {
        SystemConfig entity = findByIdOrThrow(configId);

        if (request.key() != null) {
            String normalizedKey = request.key().trim().toUpperCase();
            if (!normalizedKey.equals(entity.getKey()) && systemConfigRepository.existsByKey(normalizedKey)) {
                throw new IllegalStateException("System config key already exists: " + normalizedKey);
            }
            entity.setKey(normalizedKey);
        }
        if (request.value() != null) {
            entity.setValue(request.value().trim());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }

        return toDto(systemConfigRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(UUID configId) {
        SystemConfig entity = findByIdOrThrow(configId);
        systemConfigRepository.delete(entity);
    }

    @Override
    public SystemConfigDto getCurrentConfig() {
        SystemConfig current = systemConfigRepository.findTopByOrderByCreatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "current", "not found"));
        return toDto(current);
    }

    @Override
    public double getDefaultPlatformFeePercentage() {
        String rawValue = getRequiredConfigValue(KEY_PLATFORM_FEE_PERCENTAGE);
        return parseRequiredDouble(KEY_PLATFORM_FEE_PERCENTAGE, rawValue);
    }

    @Override
    public int getDefaultHoldingDays() {
        String rawValue = getRequiredConfigValue(KEY_HOLDING_DAYS);
        return parseRequiredInt(KEY_HOLDING_DAYS, rawValue);
    }

    @Override
    public BigDecimal getHoursPerSlot() {
        String rawValue = getRequiredConfigValue(KEY_HOURS_PER_SLOT);
        return parseRequiredBigDecimal(KEY_HOURS_PER_SLOT, rawValue);
    }

    @Override
    public String getEmailAdmin() {
        return getRequiredConfigValue(KEY_EMAIL_ADMIN);
    }

    @Override
    public int getVendorConfirmOrderHours() {
        String rawValue = getRequiredConfigValue(KEY_VENDOR_CONFIRM_ORDER_HOURS);
        return parseRequiredInt(KEY_VENDOR_CONFIRM_ORDER_HOURS, rawValue);
    }

    @Override
    public int getProductCreateRequiredExpiryMonths() {
        return getNonNegativeIntConfigOrDefault(KEY_PRODUCT_CREATE_REQUIRED_EXPIRY_MONTHS, 0);
    }

    @Override
    public int getActiveProductMinRequiredMonths() {
        return getNonNegativeIntConfigOrDefault(KEY_ACTIVE_PRODUCT_MIN_REQUIRED_MONTHS, 0);
    }

    private SystemConfig findByIdOrThrow(UUID configId) {
        return systemConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "id", configId));
    }

    private SystemConfigDto toDto(SystemConfig config) {
        return new SystemConfigDto(
                config.getConfigId(),
                config.getKey(),
                config.getValue(),
                config.getDescription(),
                config.getCreatedAt(),
                config.getUpdatedAt());
    }

    private String getRequiredConfigValue(String key) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfig::getValue)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", "key", key));
    }

    private double parseRequiredDouble(String key, String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid numeric value for system config key " + key + ": " + value);
        }
    }

    private int parseRequiredInt(String key, String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid integer value for system config key " + key + ": " + value);
        }
    }

    private int parseRequiredNonNegativeInt(String key, String value) {
        int parsed = parseRequiredInt(key, value);
        if (parsed < 0) {
            throw new IllegalStateException("System config key " + key + " must be >= 0: " + value);
        }
        return parsed;
    }

    private int getNonNegativeIntConfigOrDefault(String key, int defaultValue) {
        return systemConfigRepository.findByKey(key)
                .map(SystemConfig::getValue)
                .map(value -> parseRequiredNonNegativeInt(key, value))
                .orElseGet(() -> {
                    log.warn("System config key {} not found, using default {}", key, defaultValue);
                    return defaultValue;
                });
    }

    private BigDecimal parseRequiredBigDecimal(String key, String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid decimal value for system config key " + key + ": " + value);
        }
    }
}
