package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ReportReasonDto;
import fpt.edu.sep490.pilahub.dto.request.reportreason.CreateReportReasonRequest;
import fpt.edu.sep490.pilahub.dto.request.reportreason.UpdateReportReasonRequest;
import fpt.edu.sep490.pilahub.pojo.ReportReason;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportReasonMapper {

    ReportReasonDto toDto(ReportReason reportReason);

    @Mapping(target = "reportReasonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    ReportReason toEntity(CreateReportReasonRequest request);

    @Mapping(target = "reportReasonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget ReportReason reportReason, UpdateReportReasonRequest request);
}

