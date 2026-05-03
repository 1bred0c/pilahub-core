package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.VendorDto;
import fpt.edu.sep490.pilahub.dto.request.vendor.CreateVendorRequest;
import fpt.edu.sep490.pilahub.dto.request.vendor.UpdateVendorRequest;
import fpt.edu.sep490.pilahub.pojo.Vendor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VendorMapper {

    VendorDto toDto(Vendor vendor);

    @Mapping(target = "vendorId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "ghnShopId", ignore = true)
    Vendor toEntity(CreateVendorRequest request);

    @Mapping(target = "vendorId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "ghnShopId", ignore = true)
    void updateEntityFromRequest(UpdateVendorRequest request, @MappingTarget Vendor vendor);
}
