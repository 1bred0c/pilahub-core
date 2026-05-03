package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.LiveSessionReportDto;
import fpt.edu.sep490.pilahub.dto.request.CreateLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.pojo.LiveSessionReport;
import org.springframework.stereotype.Component;

@Component
public class LiveSessionReportMapper {

    public LiveSessionReportDto toDto(LiveSessionReport entity) {
        if (entity == null) {
            return null;
        }
        return new LiveSessionReportDto(
                entity.getLiveSessionId(),
                entity.getReporterId(),
                entity.getReportedUserId(),
                entity.getReason() != null ? entity.getReason().getCode() : null,
                entity.getReason() != null ? entity.getReason().getName() : null,
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getResolvedAt(),
                entity.getResolvedBy(),
                entity.getInternalNote()
        );
    }

    public LiveSessionReport toEntity(CreateLiveSessionReportRequest dto) {
        if (dto == null) {
            return null;
        }
        return LiveSessionReport.builder()
                .description(dto.description())
                .build();
    }
}

