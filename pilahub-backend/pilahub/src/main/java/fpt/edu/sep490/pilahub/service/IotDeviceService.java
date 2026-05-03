package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.IotDeviceDto;
import fpt.edu.sep490.pilahub.dto.request.CreateIotDeviceRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateIotDeviceRequest;

import java.util.List;
import java.util.UUID;

public interface IotDeviceService {

    IotDeviceDto createIotDevice(UUID traineeId, CreateIotDeviceRequest request);

    List<IotDeviceDto> getIotDevicesByTraineeId(UUID traineeId);

    IotDeviceDto getIotDeviceById(UUID traineeId, UUID iotDeviceId);

    IotDeviceDto updateIotDevice(UUID traineeId, UUID iotDeviceId, UpdateIotDeviceRequest request);

    void deleteIotDevice(UUID traineeId, UUID iotDeviceId);

    // Admin methods
    List<IotDeviceDto> getAllIotDevices();

    IotDeviceDto getIotDeviceByIdAdmin(UUID iotDeviceId);

    void deleteIotDeviceAdmin(UUID iotDeviceId);
}
