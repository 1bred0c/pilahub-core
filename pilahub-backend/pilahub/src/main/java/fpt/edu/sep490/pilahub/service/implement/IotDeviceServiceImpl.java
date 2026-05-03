package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.IotDeviceDto;
import fpt.edu.sep490.pilahub.dto.request.CreateIotDeviceRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateIotDeviceRequest;
import fpt.edu.sep490.pilahub.enums.IoTStatus;
import fpt.edu.sep490.pilahub.exception.DuplicateIotDeviceException;
import fpt.edu.sep490.pilahub.exception.IotDeviceNotFoundException;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.IotDeviceMapper;
import fpt.edu.sep490.pilahub.pojo.IotDevice;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.IotDeviceRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.IotDeviceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IotDeviceServiceImpl implements IotDeviceService {

    private final IotDeviceRepository iotDeviceRepository;
    private final TraineeRepository traineeRepository;
    private final IotDeviceMapper iotDeviceMapper;

    @Override
    public IotDeviceDto createIotDevice(UUID traineeId, CreateIotDeviceRequest request) {
        log.info("Creating new IoT device for trainee ID: {}", traineeId);

        // Check if trainee exists
        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        // Check if device identifier already exists
        if (iotDeviceRepository.existsByDeviceIdentifier(request.deviceIdentifier())) {
            log.error("IoT device with identifier {} already exists", request.deviceIdentifier());
            throw new DuplicateIotDeviceException("IoT device with this identifier already exists");
        }

        IotDevice iotDevice = IotDevice.builder()
                .trainee(trainee)
                .deviceName(request.deviceName())
                .iotDeviceType(request.iotDeviceType())
                .deviceIdentifier(request.deviceIdentifier())
                .connectionMethod(request.connectionMethod())
                .connectedAt(Instant.now())
                .status(IoTStatus.CONNECTED)
                .build();

        IotDevice savedDevice = iotDeviceRepository.save(iotDevice);
        log.info("IoT device created successfully with ID: {}", savedDevice.getIotDeviceId());

        return iotDeviceMapper.toDto(savedDevice);
    }

    @Override
    public List<IotDeviceDto> getIotDevicesByTraineeId(UUID traineeId) {
        log.info("Fetching IoT devices for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        List<IotDevice> devices = iotDeviceRepository.findByTrainee_TraineeId(traineeId);
        log.info("Found {} IoT device(s) for trainee ID: {}", devices.size(), traineeId);

        return devices.stream()
                .map(iotDeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IotDeviceDto getIotDeviceById(UUID traineeId, UUID iotDeviceId) {
        log.info("Fetching IoT device with ID: {} for trainee ID: {}", iotDeviceId, traineeId);

        IotDevice iotDevice = iotDeviceRepository.findByIotDeviceIdAndTrainee_TraineeId(iotDeviceId, traineeId)
                .orElseThrow(() -> {
                    log.error("IoT device not found with ID: {} for trainee ID: {}", iotDeviceId, traineeId);
                    return new IotDeviceNotFoundException("IoT device not found or does not belong to you");
                });

        return iotDeviceMapper.toDto(iotDevice);
    }

    @Override
    public IotDeviceDto updateIotDevice(UUID traineeId, UUID iotDeviceId, UpdateIotDeviceRequest request) {
        log.info("Updating IoT device with ID: {} for trainee ID: {}", iotDeviceId, traineeId);

        IotDevice iotDevice = iotDeviceRepository.findByIotDeviceIdAndTrainee_TraineeId(iotDeviceId, traineeId)
                .orElseThrow(() -> {
                    log.error("IoT device not found with ID: {} for trainee ID: {}", iotDeviceId, traineeId);
                    return new IotDeviceNotFoundException("IoT device not found or does not belong to you");
                });

        // Update only non-null fields
        if (request.deviceName() != null) {
            iotDevice.setDeviceName(request.deviceName());
        }
        if (request.iotDeviceType() != null) {
            iotDevice.setIotDeviceType(request.iotDeviceType());
        }
        if (request.connectionMethod() != null) {
            iotDevice.setConnectionMethod(request.connectionMethod());
        }
        if (request.status() != null) {
            iotDevice.setStatus(request.status());
            // Update connectedAt if status changes to CONNECTED
            if (request.status() == IoTStatus.CONNECTED) {
                iotDevice.setConnectedAt(Instant.now());
            }
        }

        // Update last sync time
        iotDevice.setLastSyncAt(Instant.now());

        IotDevice updatedDevice = iotDeviceRepository.save(iotDevice);
        log.info("IoT device updated successfully with ID: {}", iotDeviceId);

        return iotDeviceMapper.toDto(updatedDevice);
    }

    @Override
    public void deleteIotDevice(UUID traineeId, UUID iotDeviceId) {
        log.info("Deleting IoT device with ID: {} for trainee ID: {}", iotDeviceId, traineeId);

        IotDevice iotDevice = iotDeviceRepository.findByIotDeviceIdAndTrainee_TraineeId(iotDeviceId, traineeId)
                .orElseThrow(() -> {
                    log.error("IoT device not found with ID: {} for trainee ID: {}", iotDeviceId, traineeId);
                    return new IotDeviceNotFoundException("IoT device not found or does not belong to you");
                });

        iotDeviceRepository.delete(iotDevice);
        log.info("IoT device deleted successfully with ID: {}", iotDeviceId);
    }

    @Override
    public List<IotDeviceDto> getAllIotDevices() {
        log.info("Fetching all IoT devices (Admin)");

        List<IotDevice> devices = iotDeviceRepository.findAll();
        log.info("Found {} IoT device(s)", devices.size());

        return devices.stream()
                .map(iotDeviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public IotDeviceDto getIotDeviceByIdAdmin(UUID iotDeviceId) {
        log.info("Fetching IoT device with ID: {} (Admin)", iotDeviceId);

        IotDevice iotDevice = iotDeviceRepository.findById(iotDeviceId)
                .orElseThrow(() -> {
                    log.error("IoT device not found with ID: {}", iotDeviceId);
                    return new IotDeviceNotFoundException("IoT device not found with ID: " + iotDeviceId);
                });

        return iotDeviceMapper.toDto(iotDevice);
    }

    @Override
    public void deleteIotDeviceAdmin(UUID iotDeviceId) {
        log.info("Deleting IoT device with ID: {} (Admin)", iotDeviceId);

        if (!iotDeviceRepository.existsById(iotDeviceId)) {
            log.error("IoT device not found with ID: {}", iotDeviceId);
            throw new IotDeviceNotFoundException("IoT device not found with ID: " + iotDeviceId);
        }

        iotDeviceRepository.deleteById(iotDeviceId);
        log.info("IoT device deleted successfully with ID: {}", iotDeviceId);
    }
}
