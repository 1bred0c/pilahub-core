package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.VendorDto;
import fpt.edu.sep490.pilahub.dto.request.vendor.CreateVendorRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorAdminRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorRequest;

import java.util.List;
import java.util.UUID;

public interface VendorService {

    VendorDto createVendor(UUID accountId, CreateVendorRequest request);

    VendorDto getById(UUID vendorId);

    List<VendorDto> getAll();

    List<VendorDto> getAllVerified();

    List<VendorDto> searchByBusinessName(String businessName);

    List<VendorDto> getByCity(String city);

    List<VendorDto> getByCountry(String country);

    VendorDto updateVendor(UUID vendorId, UpdateVendorRequest request);

    void verifyVendor(UUID vendorId);

    void unverifyVendor(UUID vendorId);

    // ============= ADMIN OPERATIONS =============

    /**
     * Update vendor platform settings (Admin only)
     * @param vendorId Vendor ID
     * @param request Request with platform fee and holding days
     * @return Updated vendor DTO
     */
    VendorDto updateVendorPlatformSettings(UUID vendorId, UpdateVendorAdminRequest request);
}
