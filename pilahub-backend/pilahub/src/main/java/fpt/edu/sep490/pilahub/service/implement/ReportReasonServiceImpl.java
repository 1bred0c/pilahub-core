package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ReportReasonDto;
import fpt.edu.sep490.pilahub.dto.request.reportreason.CreateReportReasonRequest;
import fpt.edu.sep490.pilahub.dto.request.reportreason.UpdateReportReasonRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ReportReasonMapper;
import fpt.edu.sep490.pilahub.pojo.ReportReason;
import fpt.edu.sep490.pilahub.repository.LiveSessionReportRepository;
import fpt.edu.sep490.pilahub.repository.ReportReasonRepository;
import fpt.edu.sep490.pilahub.service.ReportReasonService;
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
public class ReportReasonServiceImpl implements ReportReasonService {

    private final ReportReasonRepository reportReasonRepository;
    private final LiveSessionReportRepository liveSessionReportRepository;
    private final ReportReasonMapper reportReasonMapper;

    @Override
    public ReportReasonDto createReportReason(CreateReportReasonRequest request) {
        String normalizedCode = normalizeCode(request.code());
        log.info("Creating report reason with code: {}", normalizedCode);

        if (reportReasonRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Report reason with name '" + request.name() + "' already exists");
        }

        if (reportReasonRepository.existsByCode(normalizedCode)) {
            throw new DuplicateResourceException("Report reason with code '" + normalizedCode + "' already exists");
        }

        ReportReason reportReason = reportReasonMapper.toEntity(request);
        reportReason.setCode(normalizedCode);
        ReportReason saved = reportReasonRepository.save(reportReason);

        return reportReasonMapper.toDto(saved);
    }

    @Override
    public ReportReasonDto getById(UUID reportReasonId) {
        ReportReason reportReason = reportReasonRepository.findById(reportReasonId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "id", reportReasonId));
        return reportReasonMapper.toDto(reportReason);
    }

    @Override
    public ReportReasonDto getByCode(String code) {
        String normalizedCode = normalizeCode(code);
        ReportReason reportReason = reportReasonRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "code", normalizedCode));
        return reportReasonMapper.toDto(reportReason);
    }

    @Override
    public List<ReportReasonDto> getAll() {
        return reportReasonRepository.findAll().stream()
                .map(reportReasonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportReasonDto> getAllActive() {
        return reportReasonRepository.findByActiveTrue().stream()
                .map(reportReasonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportReasonDto> searchByName(String name) {
        return reportReasonRepository.findByNameContainingIgnoreCase(name).stream()
                .map(reportReasonMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReportReasonDto updateReportReason(UUID reportReasonId, UpdateReportReasonRequest request) {
        ReportReason reportReason = reportReasonRepository.findById(reportReasonId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "id", reportReasonId));

        if (request.name() != null && !request.name().equals(reportReason.getName())
                && reportReasonRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Report reason with name '" + request.name() + "' already exists");
        }

        if (request.code() != null && !request.code().equals(reportReason.getCode())
                && reportReasonRepository.existsByCode(normalizeCode(request.code()))) {
            throw new DuplicateResourceException("Report reason with code '" + normalizeCode(request.code()) + "' already exists");
        }

        reportReasonMapper.updateEntity(reportReason, request);
        if (request.code() != null) {
            reportReason.setCode(normalizeCode(request.code()));
        }
        ReportReason updated = reportReasonRepository.save(reportReason);
        return reportReasonMapper.toDto(updated);
    }

    @Override
    public void deactivateReportReason(UUID reportReasonId) {
        ReportReason reportReason = reportReasonRepository.findById(reportReasonId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "id", reportReasonId));

        reportReason.setActive(false);
        reportReasonRepository.save(reportReason);
    }

    @Override
    public void deleteReportReason(UUID reportReasonId) {
        ReportReason reportReason = reportReasonRepository.findById(reportReasonId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "id", reportReasonId));

        if (!liveSessionReportRepository.findByReason(reportReason).isEmpty()) {
            throw new InvalidRequestException("Cannot delete report reason that is already used in reports");
        }

        reportReasonRepository.delete(reportReason);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}


