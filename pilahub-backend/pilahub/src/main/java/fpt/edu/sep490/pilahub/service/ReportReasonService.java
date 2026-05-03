package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ReportReasonDto;
import fpt.edu.sep490.pilahub.dto.request.reportreason.CreateReportReasonRequest;
import fpt.edu.sep490.pilahub.dto.request.reportreason.UpdateReportReasonRequest;

import java.util.List;
import java.util.UUID;

public interface ReportReasonService {

    ReportReasonDto createReportReason(CreateReportReasonRequest request);

    ReportReasonDto getById(UUID reportReasonId);

    ReportReasonDto getByCode(String code);

    List<ReportReasonDto> getAll();

    List<ReportReasonDto> getAllActive();

    List<ReportReasonDto> searchByName(String name);

    ReportReasonDto updateReportReason(UUID reportReasonId, UpdateReportReasonRequest request);

    void deactivateReportReason(UUID reportReasonId);

    void deleteReportReason(UUID reportReasonId);
}

