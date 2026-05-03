package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.RoadmapDto;
import fpt.edu.sep490.pilahub.dto.request.AcceptAIRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateRoadmapWithAIRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapWithDetailsRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapFilterRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.response.RoadmapAIResponse;
import fpt.edu.sep490.pilahub.dto.response.RoadmapWithDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RoadmapService {

    RoadmapDto createRoadmap(CreateRoadmapRequest request);

    RoadmapWithDetailsResponse createRoadmapWithDetails(CreateRoadmapWithDetailsRequest request);

    RoadmapAIResponse createRoadmapWithAI(CreateRoadmapWithAIRequest request);

    RoadmapWithDetailsResponse acceptAIGeneratedRoadmap(AcceptAIRoadmapRequest request);

    RoadmapDto getById(UUID roadmapId);

    RoadmapWithDetailsResponse getRoadmapWithDetails(UUID roadmapId);

    Page<RoadmapDto> getMyRoadmaps(RoadmapFilterRequest filter, Pageable pageable);

    RoadmapWithDetailsResponse getNewestRoadmapForTrainee();

    RoadmapWithDetailsResponse getPendingRoadmapForTrainee();

    // List<RoadmapDto> getAllActive();

    List<RoadmapDto> searchByTitle(String title);

    RoadmapDto updateRoadmap(UUID roadmapId, UpdateRoadmapRequest request);

    RoadmapWithDetailsResponse resetProgressAndReschedule(UUID roadmapId, UpdateRoadmapScheduleRequest request);

    RoadmapWithDetailsResponse rescheduleIncompleteSchedules(UUID roadmapId, UpdateRoadmapScheduleRequest request);

    RoadmapDto updateProgress(UUID roadmapId, Integer progressPercent);

    RoadmapDto updateFinalHealthProfile(UUID roadmapId, UUID finalHealthProfileId);

    UUID getInitialHealthProfileId(UUID roadmapId);

    UUID getFinalHealthProfileId(UUID roadmapId);

    RoadmapDto approveRoadmap(UUID roadmapId);

    void deactivateRoadmap(UUID roadmapId);

    void deleteRoadmap(UUID roadmapId);
}
