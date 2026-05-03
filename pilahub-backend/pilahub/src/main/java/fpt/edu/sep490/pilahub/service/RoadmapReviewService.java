package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.RoadmapReviewDto;

import java.util.UUID;

public interface RoadmapReviewService {

    RoadmapReviewDto generateReview(UUID roadmapId);

    RoadmapReviewDto getReviewByRoadmapId(UUID roadmapId);
}

