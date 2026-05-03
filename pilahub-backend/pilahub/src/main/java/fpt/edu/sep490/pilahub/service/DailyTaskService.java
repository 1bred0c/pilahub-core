package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.response.DailyTaskResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface DailyTaskService {

    DailyTaskResponse getDailyTasks(UUID traineeId, LocalDate date);
}