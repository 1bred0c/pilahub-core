package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.AddressDto;
import fpt.edu.sep490.pilahub.dto.request.CreateAddressRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateAddressRequest;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressDto createAddress(UUID traineeId, CreateAddressRequest request);

    List<AddressDto> getAddressesByTraineeId(UUID traineeId);

    AddressDto getAddressById(UUID traineeId, UUID addressId);

    AddressDto updateAddress(UUID traineeId, UUID addressId, UpdateAddressRequest request);

    void deleteAddress(UUID traineeId, UUID addressId);
}
