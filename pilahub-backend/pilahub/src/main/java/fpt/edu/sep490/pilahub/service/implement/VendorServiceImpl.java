package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.VendorDto;
import fpt.edu.sep490.pilahub.dto.request.vendor.CreateVendorRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorAdminRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.VendorMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.VendorRepository;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.service.VendorService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    private final AccountRepository accountRepository;
    private final VendorMapper vendorMapper;
    private final SystemConfigService systemConfigService;

    @Override
    public VendorDto createVendor(UUID accountId, CreateVendorRequest request) {
        log.info("Creating vendor profile for account ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (vendorRepository.existsById(accountId)) {
            throw new IllegalStateException("Vendor profile already exists for this account");
        }

        Vendor vendor = vendorMapper.toEntity(request);
        vendor.setAccount(account);
        vendor.setVerified(false);

        // Set default values for admin-controlled fields
        vendor.setPlatformFeePercentage(systemConfigService.getDefaultPlatformFeePercentage());
        vendor.setHoldingDays(systemConfigService.getDefaultHoldingDays());

        Vendor saved = vendorRepository.save(vendor);
        log.info("Successfully created vendor profile with ID: {}", saved.getVendorId());

        return vendorMapper.toDto(saved);
    }

    @Override
    public VendorDto getById(UUID vendorId) {
        log.info("Fetching vendor by ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        return vendorMapper.toDto(vendor);
    }

    @Override
    public List<VendorDto> getAll() {
        log.info("Fetching all vendors");

        return vendorRepository.findAll().stream()
                .map(vendorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorDto> getAllVerified() {
        log.info("Fetching all verified vendors");

        return vendorRepository.findByVerifiedTrue().stream()
                .map(vendorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorDto> searchByBusinessName(String businessName) {
        log.info("Searching vendors by business name: {}", businessName);

        return vendorRepository.findByBusinessNameContainingIgnoreCase(businessName).stream()
                .map(vendorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorDto> getByCity(String city) {
        log.info("Fetching vendors by city: {}", city);

        return vendorRepository.findByCity(city).stream()
                .map(vendorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<VendorDto> getByCountry(String country) {
        log.info("Fetching vendors by country: {}", country);

        return vendorRepository.findByCountry(country).stream()
                .map(vendorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public VendorDto updateVendor(UUID vendorId, UpdateVendorRequest request) {
        log.info("Updating vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendorMapper.updateEntityFromRequest(request, vendor);

        Vendor updated = vendorRepository.save(vendor);
        log.info("Successfully updated vendor with ID: {}", vendorId);

        return vendorMapper.toDto(updated);
    }

    @Override
    public void verifyVendor(UUID vendorId) {
        log.info("Verifying vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendor.setVerified(true);
        vendorRepository.save(vendor);

        log.info("Successfully verified vendor with ID: {}", vendorId);
    }

    @Override
    public void unverifyVendor(UUID vendorId) {
        log.info("Unverifying vendor with ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        vendor.setVerified(false);
        vendorRepository.save(vendor);

        log.info("Successfully unverified vendor with ID: {}", vendorId);
    }

    // ============= ADMIN OPERATIONS =============

    @Override
    public VendorDto updateVendorPlatformSettings(UUID vendorId, UpdateVendorAdminRequest request) {
        log.info("Admin updating platform settings for vendor ID: {}", vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        if (request.platformFeePercentage() != null) {
            vendor.setPlatformFeePercentage(request.platformFeePercentage());
            log.info("Updated platform fee percentage to {}% for vendor ID: {}",
                    request.platformFeePercentage(), vendorId);
        }

        if (request.holdingDays() != null) {
            vendor.setHoldingDays(request.holdingDays());
            log.info("Updated holding days to {} for vendor ID: {}",
                    request.holdingDays(), vendorId);
        }

        Vendor updated = vendorRepository.save(vendor);
        log.info("Successfully updated platform settings for vendor ID: {}", vendorId);

        return vendorMapper.toDto(updated);
    }
}