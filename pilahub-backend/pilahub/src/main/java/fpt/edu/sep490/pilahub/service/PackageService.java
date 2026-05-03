package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PackageDto;
import fpt.edu.sep490.pilahub.dto.request.CreatePackageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdatePackageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PackageService {

    /**
     * Create a new package (Admin only)
     * @param request create package request
     * @return created package DTO
     */
    PackageDto createPackage(CreatePackageRequest request);

    /**
     * Get all packages with pagination (Admin only)
     * @param pageable pagination parameters
     * @return paginated list of packages
     */
    Page<PackageDto> getAllPackages(Pageable pageable);

    /**
     * Get all active packages (Available for all users)
     * @return list of active packages
     */
    List<PackageDto> getActivePackages();

    /**
     * Get package by ID
     * @param packageId package ID
     * @return package DTO
     */
    PackageDto getPackageById(UUID packageId);

    /**
     * Update package by ID (Admin only)
     * @param packageId package ID
     * @param request update request
     * @return updated package DTO
     */
    PackageDto updatePackage(UUID packageId, UpdatePackageRequest request);

    /**
     * Delete package by ID (Admin only) - soft delete by setting active to false
     * @param packageId package ID
     */
    void deletePackage(UUID packageId);

    /**
     * Activate package by ID (Admin only)
     * @param packageId package ID
     * @return updated package DTO
     */
    PackageDto activatePackage(UUID packageId);
}
