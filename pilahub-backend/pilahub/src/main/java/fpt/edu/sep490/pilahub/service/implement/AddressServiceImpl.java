package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.AddressDto;
import fpt.edu.sep490.pilahub.dto.request.CreateAddressRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateAddressRequest;
import fpt.edu.sep490.pilahub.exception.AddressNotFoundException;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.AddressMapper;
import fpt.edu.sep490.pilahub.pojo.Address;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.AddressRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final TraineeRepository traineeRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressDto createAddress(UUID traineeId, CreateAddressRequest request) {
        log.info("Creating new address for trainee ID: {}", traineeId);

        // Check if trainee exists
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        // Handle default address logic
        boolean shouldBeDefault = request.isDefault() != null && request.isDefault();

        // If this should be default, unset any existing default
        if (shouldBeDefault) {
            addressRepository.findByTrainee_TraineeIdAndDefaultTrue(traineeId)
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                        log.info("Unset previous default address for trainee ID: {}", traineeId);
                    });
        } else {
            // If no addresses exist yet, make this the default
            List<Address> existingAddresses = addressRepository.findByTrainee_TraineeId(traineeId);
            if (existingAddresses.isEmpty()) {
                shouldBeDefault = true;
                log.info("First address for trainee, setting as default");
            }
        }

        Address address = Address.builder()
                .trainee(trainee)
                .receiverName(request.receiverName())
                .receiverPhone(request.receiverPhone())
                .addressLine(request.addressLine())
                .province(request.province())
                .city(request.city())
                .district(request.district())
                .ward(request.ward())
                .isDefault(shouldBeDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {}", savedAddress.getAddressId());

        return addressMapper.toDto(savedAddress);
    }

    @Override
    public List<AddressDto> getAddressesByTraineeId(UUID traineeId) {
        log.info("Fetching addresses for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        List<Address> addresses = addressRepository.findByTrainee_TraineeId(traineeId);
        log.info("Found {} address(es) for trainee ID: {}", addresses.size(), traineeId);

        return addresses.stream()
                .map(addressMapper::toDto)
                .toList();
    }

    @Override
    public AddressDto getAddressById(UUID traineeId, UUID addressId) {
        log.info("Fetching address ID: {} for trainee ID: {}", addressId, traineeId);

        Address address = addressRepository.findByAddressIdAndTrainee_TraineeId(addressId, traineeId)
                .orElseThrow(() -> {
                    log.error("Address not found with ID: {} for trainee ID: {}", addressId, traineeId);
                    return new AddressNotFoundException("Address not found with ID: " + addressId);
                });

        return addressMapper.toDto(address);
    }

    @Override
    public AddressDto updateAddress(UUID traineeId, UUID addressId, UpdateAddressRequest request) {
        log.info("Updating address ID: {} for trainee ID: {}", addressId, traineeId);

        Address address = addressRepository.findByAddressIdAndTrainee_TraineeId(addressId, traineeId)
                .orElseThrow(() -> {
                    log.error("Address not found with ID: {} for trainee ID: {}", addressId, traineeId);
                    return new AddressNotFoundException("Address not found with ID: " + addressId);
                });

        // Update only non-null fields
        if (request.receiverName() != null) {
            address.setReceiverName(request.receiverName());
        }
        if (request.receiverPhone() != null) {
            address.setReceiverPhone(request.receiverPhone());
        }
        if (request.addressLine() != null) {
            address.setAddressLine(request.addressLine());
        }
        if (request.province() != null) {
            address.setProvince(request.province());
        }
        if (request.city() != null) {
            address.setCity(request.city());
        }
        if (request.district() != null) {
            address.setDistrict(request.district());
        }
        if (request.ward() != null) {
            address.setWard(request.ward());
        }

        // Handle default address update
        if (request.isDefault() != null) {
            boolean shouldBeDefault = request.isDefault();

            // If setting this address as default
            if (shouldBeDefault && !address.isDefault()) {
                // Unset any existing default
                addressRepository.findByTrainee_TraineeIdAndDefaultTrue(traineeId)
                        .ifPresent(existingDefault -> {
                            if (!existingDefault.getAddressId().equals(addressId)) {
                                existingDefault.setDefault(false);
                                addressRepository.save(existingDefault);
                                log.info("Unset previous default address for trainee ID: {}", traineeId);
                            }
                        });
                address.setDefault(true);
            } else if (!shouldBeDefault && address.isDefault()) {
                // Cannot unset default if it's the only address or if there are other addresses
                List<Address> allAddresses = addressRepository.findByTrainee_TraineeId(traineeId);
                if (allAddresses.size() > 1) {
                    address.setDefault(false);
                    // Set another address as default
                    allAddresses.stream()
                            .filter(a -> !a.getAddressId().equals(addressId))
                            .findFirst()
                            .ifPresent(newDefault -> {
                                newDefault.setDefault(true);
                                addressRepository.save(newDefault);
                                log.info("Set new default address ID: {} for trainee ID: {}",
                                        newDefault.getAddressId(), traineeId);
                            });
                } else {
                    log.warn("Cannot unset default for the only address of trainee ID: {}", traineeId);
                }
            }
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully with ID: {}", addressId);

        return addressMapper.toDto(updatedAddress);
    }

    @Override
    public void deleteAddress(UUID traineeId, UUID addressId) {
        log.info("Deleting address ID: {} for trainee ID: {}", addressId, traineeId);

        Address address = addressRepository.findByAddressIdAndTrainee_TraineeId(addressId, traineeId)
                .orElseThrow(() -> {
                    log.error("Address not found with ID: {} for trainee ID: {}", addressId, traineeId);
                    return new AddressNotFoundException("Address not found with ID: " + addressId);
                });

        // If deleting the default address, set another as default
        if (address.isDefault()) {
            List<Address> remainingAddresses = addressRepository.findByTrainee_TraineeId(traineeId)
                    .stream()
                    .filter(a -> !a.getAddressId().equals(addressId))
                    .toList();

            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
                log.info("Set new default address ID: {} for trainee ID: {}",
                        newDefault.getAddressId(), traineeId);
            }
        }

        addressRepository.delete(address);
        log.info("Address deleted successfully with ID: {}", addressId);
    }
}
