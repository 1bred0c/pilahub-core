package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PackageDto;
import fpt.edu.sep490.pilahub.dto.request.CreatePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdatePackageRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PackageMapper;
import fpt.edu.sep490.pilahub.pojo.Package;
import fpt.edu.sep490.pilahub.repository.PackageRepository;
import fpt.edu.sep490.pilahub.service.PackageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;

    @Override
    public PackageDto createPackage(CreatePackageRequest request) {
        log.info("Creating new package with name: {}", request.packageName());

        // Check if package name already exists
        if (packageRepository.existsByPackageName(request.packageName())) {
            log.error("Package name already exists: {}", request.packageName());
            throw new IllegalArgumentException("Package name already exists");
        }

        Package packageEntity = Package.builder()
                .packageName(request.packageName())
                .description(request.description())
                .price(request.price())
                .durationInDays(request.durationInDays())
                .packageType(request.packageType())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .build();

        Package savedPackage = packageRepository.save(packageEntity);
        log.info("Package created successfully with ID: {}", savedPackage.getPackageId());

        return packageMapper.toDto(savedPackage);
    }

    @Override
    public Page<PackageDto> getAllPackages(Pageable pageable) {
        log.info("Fetching all packages with pagination - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Package> packagePage = packageRepository.findAll(pageable);
        log.info("Found {} total packages, returning page {} with {} elements",
                packagePage.getTotalElements(),
                packagePage.getNumber(),
                packagePage.getNumberOfElements());

        return packagePage.map(packageMapper::toDto);
    }

    @Override
    public List<PackageDto> getActivePackages() {
        log.info("Fetching all active packages");

        List<Package> activePackages = packageRepository.findByIsActiveTrue();
        log.info("Found {} active packages", activePackages.size());

        return activePackages.stream()
                .map(packageMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PackageDto getPackageById(UUID packageId) {
        log.info("Fetching package with ID: {}", packageId);

        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", packageId);
                    return new ResourceNotFoundException("Package", "id", packageId);
                });

        return packageMapper.toDto(packageEntity);
    }

    @Override
    public PackageDto updatePackage(UUID packageId, UpdatePackageRequest request) {
        log.info("Updating package with ID: {}", packageId);

        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", packageId);
                    return new ResourceNotFoundException("Package", "id", packageId);
                });

        // Update only non-null fields
        if (request.packageName() != null) {
            // Check if package name is already taken by another package
            if (!request.packageName().equals(packageEntity.getPackageName()) &&
                packageRepository.existsByPackageName(request.packageName())) {
                log.error("Package name already exists: {}", request.packageName());
                throw new IllegalArgumentException("Package name already exists");
            }
            packageEntity.setPackageName(request.packageName());
        }

        if (request.description() != null) {
            packageEntity.setDescription(request.description());
        }

        if (request.price() != null) {
            packageEntity.setPrice(request.price());
        }

        if (request.durationInDays() != null) {
            packageEntity.setDurationInDays(request.durationInDays());
        }

        if (request.packageType() != null) {
            packageEntity.setPackageType(request.packageType());
        }

        if (request.isActive() != null) {
            packageEntity.setIsActive(request.isActive());
        }

        Package updatedPackage = packageRepository.save(packageEntity);
        log.info("Package updated successfully with ID: {}", packageId);

        return packageMapper.toDto(updatedPackage);
    }

    @Override
    public void deletePackage(UUID packageId) {
        log.info("Deactivating package with ID: {}", packageId);

        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", packageId);
                    return new ResourceNotFoundException("Package", "id", packageId);
                });

        packageEntity.setIsActive(false);
        packageRepository.save(packageEntity);
        log.info("Package deactivated successfully with ID: {}", packageId);
    }

    @Override
    public PackageDto activatePackage(UUID packageId) {
        log.info("Activating package with ID: {}", packageId);

        Package packageEntity = packageRepository.findById(packageId)
                .orElseThrow(() -> {
                    log.error("Package not found with ID: {}", packageId);
                    return new ResourceNotFoundException("Package", "id", packageId);
                });

        packageEntity.setIsActive(true);
        Package updatedPackage = packageRepository.save(packageEntity);
        log.info("Package activated successfully with ID: {}", packageId);

        return packageMapper.toDto(updatedPackage);
    }
}
