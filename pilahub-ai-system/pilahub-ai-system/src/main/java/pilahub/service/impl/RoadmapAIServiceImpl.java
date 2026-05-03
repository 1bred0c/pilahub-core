package pilahub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pilahub.config.GeminiConfig;
import pilahub.dto.request.ExerciseAIRequest;
import pilahub.dto.request.InjuryAIRequest;
import pilahub.dto.request.IngredientAIRequest;
import pilahub.dto.request.IngredientRuleAIRequest;
import pilahub.dto.request.RoadmapAIRequest;
import pilahub.dto.request.SupplementAIRequest;
import pilahub.dto.response.RoadmapAIResponse;
import pilahub.enums.AIModel;
import pilahub.service.GeminiFileStoreService;
import pilahub.service.RoadmapAIService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoadmapAIServiceImpl implements RoadmapAIService {

    private final Client geminiClient;
    private final GeminiConfig geminiConfig;
    private final ObjectMapper objectMapper;
    private final GeminiFileStoreService fileStoreService;

    @Override
    public RoadmapAIResponse generateRoadmap(RoadmapAIRequest request) {
        try {
            log.info(
                    "Starting roadmap generation for primary goal: {}, secondary goals: {}, duration: {} weeks, training days: {}",
                    request.primaryGoal(), request.secondaryGoals(), request.durationWeeks(),
                    request.trainingDays().size());

            // Step 1: Try to get reference document URI from File Store
            String documentUri = null;
            try {
                documentUri = fileStoreService.getActiveRoadmapDocumentUri();
                if (documentUri != null) {
                    log.info("Using reference document from File Store: {}", documentUri);
                } else {
                    log.info("No active reference document found, generating roadmap without document");
                }
            } catch (Exception e) {
                log.warn("Error getting reference document from File Store, proceeding without document: {}",
                        e.getMessage());
            }

            // Step 2: Generate roadmap with or without document
            return generateRoadmapWithDocument(request, documentUri);

        } catch (Exception e) {
            log.error("Error during roadmap generation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate roadmap: " + e.getMessage(), e);
        }
    }

    @Override
    public RoadmapAIResponse generateRoadmapWithDocument(RoadmapAIRequest request, String documentUri) {
        try {
            log.info("Generating roadmap with document URI: {}", documentUri != null ? documentUri : "none");

            // Step 1: Build prompt
            String prompt = buildRoadmapPrompt(request);

            // Step 2: Add document reference if available
            if (documentUri != null) {
                prompt = "📎 Reference Document: " + documentUri + "\n\n" +
                        "Please use the reference document above as a guide for creating the workout roadmap. " +
                        "Follow the structure, principles, and guidelines from the document while adapting them to the specific user profile below.\n\n"
                        +
                        prompt;
                log.debug("Added document reference to prompt");
            }

            log.debug("Prompt built successfully, length: {} characters", prompt.length());

            // Step 3: Call Gemini API
            String rawResponse = callGeminiAPI(prompt);

            // Step 4: Parse and validate response
            RoadmapAIResponse response = parseRoadmapResponse(rawResponse);

            log.info("Roadmap generation completed successfully. Stages: {}, Confidence: {}",
                    response.stages() != null ? response.stages().size() : 0,
                    response.confidenceScore());

            return response;

        } catch (Exception e) {
            log.error("Error during roadmap generation with document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate roadmap: " + e.getMessage(), e);
        }
    }

    @Override
    public String buildRoadmapPrompt(RoadmapAIRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append(
                "You are an expert fitness coach and workout program designer with extensive knowledge in exercise science, anatomy, and progressive training methodologies.\n\n");

        prompt.append("# TASK\n");
        prompt.append(
                "Create a comprehensive workout roadmap based on the user profile below. Return the response in JSON format according to the specified schema.\n");
        prompt.append(
                "IMPORTANT: All content MUST be written in VIETNAMESE language only. This includes all descriptions, notes, instructions, and text fields.\n\n");

        prompt.append("# USER PROFILE\n");
        prompt.append("- Primary Goal: ").append(request.primaryGoal()).append("\n");
        if (request.secondaryGoals() != null && !request.secondaryGoals().isEmpty()) {
            prompt.append("- Secondary Goals: ").append(String.join(", ", request.secondaryGoals())).append("\n");
        }
        prompt.append("- Age: ").append(request.age()).append(" years\n");
        prompt.append("- Gender: ").append(request.gender()).append("\n");
        prompt.append("- Workout Level: ").append(getWorkoutLevelDescription(request.workoutLevel())).append("\n");
        prompt.append("- Workout Frequency: ").append(getWorkoutFrequencyDescription(request.workoutFrequency()))
                .append("\n");
        prompt.append("- Training Days: ").append(String.join(", ", request.trainingDays())).append("\n");
        prompt.append("- Duration: ").append(request.durationWeeks()).append(" weeks\n");

        if (request.heightCm() != null && request.weightKg() != null) {
            prompt.append("- Height: ").append(request.heightCm()).append(" cm\n");
            prompt.append("- Weight: ").append(request.weightKg()).append(" kg\n");

            // Calculate BMI
            double heightInMeters = request.heightCm().doubleValue() / 100.0;
            double bmi = request.weightKg().doubleValue() / (heightInMeters * heightInMeters);
            prompt.append("- BMI: ").append(String.format("%.1f", bmi)).append("\n");
        }

        if (request.injuries() != null && !request.injuries().isEmpty()) {
            prompt.append("\n## INJURIES & HEALTH CONSIDERATIONS\n");
            for (InjuryAIRequest injury : request.injuries()) {
                prompt.append("- ").append(injury.name())
                        .append(" (Status: ").append(injury.status()).append(")\n");
                if (injury.description() != null) {
                    prompt.append("  Description: ").append(injury.description()).append("\n");
                }
                if (injury.symptoms() != null) {
                    prompt.append("  Symptoms: ").append(injury.symptoms()).append("\n");
                }
                if (injury.affectedBodyParts() != null && !injury.affectedBodyParts().isEmpty()) {
                    prompt.append("  Affected Areas: ");
                    injury.affectedBodyParts().forEach(part -> prompt.append(part.name()).append(", "));
                    prompt.setLength(prompt.length() - 2);
                    prompt.append("\n");
                }
                if (injury.preventionTips() != null) {
                    prompt.append("  Prevention: ").append(injury.preventionTips()).append("\n");
                }
            }
        }

        // Add available exercises section
        if (request.availableExercises() != null && !request.availableExercises().isEmpty()) {
            prompt.append("\n# AVAILABLE EXERCISES\n");
            prompt.append(
                    "CRITICAL: You MUST ONLY use exercises from this list. DO NOT create or suggest any exercises not listed here.\n");
            prompt.append("The following exercises have been pre-approved for this user's profile and goals:\n\n");

            for (ExerciseAIRequest exercise : request.availableExercises()) {
                if (exercise == null || exercise.name() == null || exercise.name().trim().isEmpty()) {
                    continue;
                }

                prompt.append("- ").append(exercise.name());
                if (exercise.exerciseType() != null && !exercise.exerciseType().trim().isEmpty()) {
                    prompt.append(" (Type: ").append(exercise.exerciseType()).append(")");
                }
                if (exercise.difficultyLevel() != null && !exercise.difficultyLevel().trim().isEmpty()) {
                    prompt.append(" [Level: ").append(exercise.difficultyLevel()).append("]");
                }
                prompt.append("\n");
            }
            prompt.append("\n");
        }

        // Add available supplements section
        if (request.availableSupplements() != null && !request.availableSupplements().isEmpty()) {
            prompt.append("\n# AVAILABLE SUPPLEMENTS\n");
            prompt.append(
                    "CRITICAL: You MUST ONLY recommend supplements from this list. DO NOT create or suggest any supplements not listed here.\n");
            prompt.append(
                    "The following supplements have been pre-filtered based on user profile, safety rules, and fitness goal.\n\n");

            for (SupplementAIRequest supplement : request.availableSupplements()) {
                prompt.append("## ").append(supplement.name()).append("\n");
                if (supplement.brand() != null) {
                    prompt.append("Brand: ").append(supplement.brand()).append("\n");
                }
                if (supplement.description() != null) {
                    prompt.append("Description: ").append(supplement.description()).append("\n");
                }
                if (supplement.benefits() != null) {
                    prompt.append("Benefits: ").append(supplement.benefits()).append("\n");
                }
                if (supplement.purposes() != null && !supplement.purposes().isEmpty()) {
                    prompt.append("Primary Purposes: ").append(String.join(", ", supplement.purposes())).append("\n");
                }
                if (supplement.usageInstructions() != null) {
                    prompt.append("Usage: ").append(supplement.usageInstructions()).append("\n");
                }

                // Add ingredient information
                if (supplement.ingredients() != null && !supplement.ingredients().isEmpty()) {
                    prompt.append("Key Ingredients:\n");
                    for (IngredientAIRequest ingredient : supplement.ingredients()) {
                        prompt.append("  - ").append(ingredient.name());
                        if (ingredient.amount() != null) {
                            prompt.append(" (").append(ingredient.amount()).append(" ").append(ingredient.unit())
                                    .append(")");
                        }
                        prompt.append("\n");
                    }
                }

                prompt.append("\n");
            }
        }

        prompt.append("\n# REQUIRED JSON OUTPUT FORMAT\n");
        prompt.append("Return EXACTLY in this JSON format:\n\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"<concise roadmap title>\",\n");
        prompt.append("  \"description\": \"<comprehensive roadmap description>\",\n");
        prompt.append("  \"stages\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"stageName\": \"<stage name, e.g., Foundation Phase>\",\n");
        prompt.append("      \"description\": \"<stage description>\",\n");
        prompt.append("      \"stageOrder\": 1,\n");
        prompt.append("      \"durationWeeks\": <number of weeks>,\n");
        prompt.append("      \"schedules\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"scheduleName\": \"<workout name, e.g., Upper Body Strength>\",\n");
        prompt.append("          \"description\": \"<workout description>\",\n");
        prompt.append("          \"dayOfWeek\": \"<MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY>\",\n");
        prompt.append("          \"durationMinutes\": <total workout duration in minutes>,\n");
        prompt.append("          \"exercises\": [\n");
        prompt.append("            {\n");
        prompt.append("              \"exerciseName\": \"<exercise name>\",\n");
        prompt.append("              \"exerciseOrder\": 1,\n");
        prompt.append("              \"sets\": <number of sets>,\n");
        prompt.append("              \"reps\": <number of reps>,\n");
        prompt.append(
                "              \"durationSeconds\": <estimated active time per set in seconds, MUST be > 0>,\n");
        prompt.append("              \"restSeconds\": <rest between sets in seconds>,\n");
        prompt.append("              \"notes\": \"<form tips, rest time, progression notes>\"\n");
        prompt.append("            }\n");
        prompt.append("          ]\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"supplementRecommendations\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"supplementName\": \"<exact name from available supplements list>\",\n");
        prompt.append(
                "          \"recommendedTiming\": \"<when to take, e.g., Post-workout, Morning, Before bed>\",\n");
        prompt.append("          \"dosage\": \"<amount per serving, e.g., 25-30g per serving>\",\n");
        prompt.append(
                "          \"reason\": \"<why this supplement for this stage, e.g., Supports muscle recovery>\",\n");
        prompt.append("          \"priority\": \"<HIGH|MEDIUM|LOW>\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"notes\": \"<stage-specific tips or considerations>\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"confidenceScore\": <number from 0.0 to 1.0>,\n");
        prompt.append("  \"notes\": \"<overall program notes or warnings>\"\n");
        prompt.append("}\n\n");

        prompt.append("# ROADMAP DESIGN GUIDELINES\n");
        prompt.append("1. **Progressive Overload**: Gradually increase intensity across stages\n");
        prompt.append("2. **Periodization**: Divide the roadmap into ")
                .append(calculateOptimalStages(request.durationWeeks())).append(" stages\n");
        prompt.append("3. **Training Days**: Distribute workouts ONLY on: ")
                .append(String.join(", ", request.trainingDays())).append("\n");
        prompt.append("4. **Exercise Selection**: Choose exercises appropriate for ").append(request.workoutLevel())
                .append(" level\n");
        prompt.append("5. **Recovery**: Include appropriate rest between similar muscle groups\n");
        prompt.append("6. **Safety**: AVOID exercises that stress injured body parts\n");
        prompt.append("7. **Goal Alignment**:\n");
        prompt.append("   - PRIMARY: All exercises MUST support the primary goal: ").append(request.primaryGoal())
                .append("\n");
        if (request.secondaryGoals() != null && !request.secondaryGoals().isEmpty()) {
            prompt.append("   - SECONDARY: Include exercises for secondary goals (")
                    .append(String.join(", ", request.secondaryGoals()))
                    .append(") ONLY if they do not conflict with the primary goal's safety and training focus\n");
        }
        prompt.append(
                "   - RULE: Always satisfy Primary Goal first. Secondary Goals are only applied if they do not conflict with the Primary Goal's safety and training focus.\n\n");

        prompt.append("# STAGE STRUCTURE RULES\n");
        prompt.append("- Each stage should be 3-6 weeks long\n");
        prompt.append("- Total of all stage durations must equal ").append(request.durationWeeks()).append(" weeks\n");
        prompt.append("- Stage names should reflect training focus (e.g., Foundation, Hypertrophy, Strength, Peak)\n");
        prompt.append("- Each stage must have schedules for ALL training days: ")
                .append(String.join(", ", request.trainingDays())).append("\n\n");

        prompt.append("# SCHEDULE (PILATES WORKOUT) RULES\n");
        prompt.append("- Each schedule represents ONE Pilates workout session on a specific training day\n");
        prompt.append("- dayOfWeek must be one of the provided training days\n");
        prompt.append(
                "- scheduleName should reflect the Pilates focus (e.g., 'Core Stability Flow', 'Full Body Mobility', 'Posture Alignment', 'Glute & Core Control')\n");
        prompt.append(
                "- Pilates sessions should feel like a continuous mindful movement flow, not gym-style split training\n");
        prompt.append("- Number of exercises per schedule MUST depend on workoutLevel:\n");
        prompt.append("  * Beginner: 4-6 exercises\n");
        prompt.append("  * Intermediate: 6-8 exercises\n");
        prompt.append("  * Advanced: 8-10 exercises\n");
        prompt.append(
                "- durationMinutes should represent an estimated practical session length, not an exact second-by-second calculation\n");
        prompt.append(
                "- Calculate total session time based on all exercise active time, set repetitions, transitions, and rest periods, then round to the nearest practical Pilates session block\n");
        prompt.append(
                "- Practical rounded durationMinutes should normally be one of: 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 75, 90\n");
        prompt.append(
                "- Small deviations between calculated total time and durationMinutes are acceptable as long as the session remains realistically close\n");
        prompt.append("- Example: if calculated total time is around 27-29 minutes, durationMinutes should be 30\n");
        prompt.append("- Example: if calculated total time is around 43-47 minutes, durationMinutes should be 45\n\n");

        prompt.append("# EXERCISE RULES\n");
        if (request.availableExercises() != null && !request.availableExercises().isEmpty()) {
            prompt.append("CRITICAL: exerciseName MUST be selected ONLY from the 'AVAILABLE EXERCISES' list above.\n");
            prompt.append("PROHIBITED: Do NOT invent, create, or suggest exercises outside the provided list.\n");
        }
        prompt.append(
                "- exerciseOrder starts at 1 for each schedule and should create a smooth Pilates progression:\n");
        prompt.append("  breathing/warm-up -> core activation -> mobility/control -> endurance hold -> cool down\n");
        prompt.append(
                "- Pilates exercises are primarily duration-based and control-focused, not heavy rep-based strength training\n");
        prompt.append("- sets: REQUIRED and MUST be > 0\n");
        prompt.append(
                "- reps: REQUIRED and MUST be > 0 if the movement is repetition-based; for static hold movements use small realistic reps value if needed for schema consistency\n");
        prompt.append(
                "- durationSeconds: REQUIRED and MUST be > 0, and must reflect realistic Pilates execution time according to workoutLevel:\n");
        prompt.append("  * Static hold / isometric control exercises: usually 30-120 seconds\n");
        prompt.append("  * Slow controlled dynamic flow exercises: usually 120-300 seconds\n");
        prompt.append("  * Beginner durations should stay on the lower end, Advanced can stay longer\n");
        prompt.append("- restSeconds: REQUIRED and MUST be > 0, but Pilates rest should stay short and mindful:\n");
        prompt.append("  usually 15-45 seconds depending on difficulty\n");
        prompt.append("- Exercise duration and rest duration MUST scale naturally with workoutLevel:\n");
        prompt.append("  Beginner = shorter active time + slightly more rest\n");
        prompt.append("  Intermediate = moderate active time + moderate rest\n");
        prompt.append("  Advanced = longer active time + shorter rest\n");
        prompt.append(
                "- The sum of all exercise durations and rests MUST align with the full schedule durationMinutes realistically\n");
        prompt.append(
                "- notes should include breathing cues, posture/form alignment, muscle engagement focus, and smooth transition guidance\n\n");

        prompt.append("# TIME CONSISTENCY VALIDATION\n");
        prompt.append("- Before generating final output, estimate totalSessionSeconds using:\n");
        prompt.append("  sum(all exercise durationSeconds * sets) + estimated transition time + rest periods\n");
        prompt.append(
                "- durationMinutes should be the nearest practical rounded session duration to the estimated total time\n");
        prompt.append("- Avoid awkward exact durations such as 28, 33, or 47 unless absolutely necessary\n");
        prompt.append("- Prefer user-friendly rounded durations that resemble real Pilates class planning\n\n");

        // Add supplement recommendation rules
        if (request.availableSupplements() != null && !request.availableSupplements().isEmpty()) {
            prompt.append("# SUPPLEMENT RECOMMENDATION RULES\n");
            prompt.append("CRITICAL: You MUST follow these rules when recommending supplements:\n");
            prompt.append(
                    "1. **Only Recommend Listed Supplements**: Choose ONLY from the 'AVAILABLE SUPPLEMENTS' section above\n");
            prompt.append(
                    "2. **Use Exact Names**: supplementName must match EXACTLY as written in the available list\n");
            prompt.append(
                    "   PROHIBITED: Do NOT create, invent, or suggest any supplements not in the provided list\n");
            prompt.append("3. **Stage-Specific Recommendations**: Tailor supplements to each stage's training focus\n");
            prompt.append("4. **Priority Levels**:\n");
            prompt.append("   - HIGH: Essential for the goal and stage (e.g., protein for muscle building)\n");
            prompt.append("   - MEDIUM: Beneficial but not essential (e.g., BCAAs during training)\n");
            prompt.append("   - LOW: Optional, provides marginal benefits\n");
            prompt.append(
                    "5. **Timing Specificity**: Be specific about when to take (e.g., 'Within 30 min post-workout', 'Morning with breakfast')\n");
            prompt.append(
                    "6. **Dosage Clarity**: Provide clear dosage amounts based on the supplement's usage instructions\n");
            prompt.append("7. **Reason Justification**: Explain WHY this supplement helps THIS specific stage\n");
            prompt.append(
                    "8. **Progressive Recommendations**: Early stages may have fewer supplements, later stages may add more\n");
            prompt.append(
                    "9. **Not Mandatory**: If no suitable supplements exist for a stage, leave supplementRecommendations as empty array []\n");
            prompt.append(
                    "10. **Consider Training Intensity**: Recommend more recovery supplements during high-intensity stages\n\n");
        }

        prompt.append("# WORKOUT LEVEL CONSIDERATIONS\n");
        switch (request.workoutLevel().name()) {
            case "BEGINNER":
                prompt.append("- Focus on basic breathing, posture alignment, and core activation\n");
                prompt.append("- Use simple low-complexity Pilates movements\n");
                prompt.append("- Shorter hold durations and longer rest periods\n");
                prompt.append("- Prioritize movement control over intensity\n");
                break;
            case "INTERMEDIATE":
                prompt.append("- Use moderately challenging full-body control movements\n");
                prompt.append("- Increase coordination, balance, and endurance demands\n");
                prompt.append("- Moderate hold durations and moderate rest periods\n");
                prompt.append("- Introduce longer controlled movement flows\n");
                break;
            case "ADVANCED":
                prompt.append("- Use high-control, high-endurance Pilates sequences\n");
                prompt.append("- Longer hold durations, stronger core endurance, and complex transitions\n");
                prompt.append("- Shorter rest periods with sustained movement precision\n");
                prompt.append("- Emphasize deep stability, flexibility, and muscular control\n");
                break;
        }
        prompt.append("\n");

        prompt.append("# INJURY ACCOMMODATIONS\n");
        if (request.injuries() != null && !request.injuries().isEmpty()) {
            prompt.append("CRITICAL: The user has the following injuries. You MUST modify the program accordingly:\n");
            for (InjuryAIRequest injury : request.injuries()) {
                prompt.append("- ").append(injury.name()).append(" (Status: ").append(injury.status()).append(")\n");
                if (injury.symptoms() != null) {
                    prompt.append("  Symptoms: ").append(injury.symptoms()).append("\n");
                }
                prompt.append("  Avoid exercises that load/stress: ");
                if (injury.affectedBodyParts() != null && !injury.affectedBodyParts().isEmpty()) {
                    injury.affectedBodyParts().forEach(part -> prompt.append(part.name()).append(", "));
                    prompt.setLength(prompt.length() - 2);
                }
                prompt.append("\n");
                if (injury.preventionTips() != null) {
                    prompt.append("  Prevention tips: ").append(injury.preventionTips()).append("\n");
                }
            }
        } else {
            prompt.append("No current injuries reported. Include standard injury prevention practices.\n");
        }
        prompt.append("\n");

        prompt.append("# IMPORTANT NOTES\n");
        prompt.append("- Return ONLY the JSON object, no additional text before or after\n");
        prompt.append("- Ensure JSON is valid and parseable\n");
        prompt.append(
                "- LANGUAGE REQUIREMENT: ALL text content (title, description, notes, stage names, schedule names, exercise notes, etc.) MUST be written in VIETNAMESE only\n");
        prompt.append(
                "- Day names in 'dayOfWeek' field MUST use Vietnamese format: THỨ HAI, THỨ BA, THỨ TƯ, THỨ NĂM, THỨ SÁU, THỨ BẢY, CHỦ NHẬT\n");
        if (request.availableExercises() != null && !request.availableExercises().isEmpty()) {
            prompt.append("- Exercise names MUST match EXACTLY with names from AVAILABLE EXERCISES list\n");
            prompt.append("- NEVER generate, create, or invent exercise names that are not in the provided list\n");
        }
        if (request.availableSupplements() != null && !request.availableSupplements().isEmpty()) {
            prompt.append(
                    "- Supplement names in supplementRecommendations MUST match exactly with names from AVAILABLE SUPPLEMENTS list\n");
            prompt.append("- NEVER generate, create, or invent supplement names that are not in the provided list\n");
            prompt.append(
                    "- Each stage can have 0-5 supplement recommendations based on training intensity and goals\n");
        }
        prompt.append("- Be specific in exercise descriptions and notes\n");
        prompt.append(
                "- For each exercise, ALWAYS provide suitable values for sets, reps, durationSeconds, and restSeconds\n");
        prompt.append("- NEVER use 0 or negative values for sets, reps, durationSeconds, or restSeconds\n");
        prompt.append("- confidenceScore should reflect certainty in the program (0.7-0.95 typical)\n");
        prompt.append("\n");

        prompt.append("# PROHIBITION WARNING\n");
        prompt.append("⚠️ STRICTLY FORBIDDEN:\n");
        if (request.availableExercises() != null && !request.availableExercises().isEmpty()) {
            prompt.append("- DO NOT create or suggest exercises outside the AVAILABLE EXERCISES list\n");
        }
        if (request.availableSupplements() != null && !request.availableSupplements().isEmpty()) {
            prompt.append("- DO NOT create or suggest supplements outside the AVAILABLE SUPPLEMENTS list\n");
        }
        prompt.append("- Only use the exact names provided in the lists above\n");
        prompt.append("\n");

        prompt.append("BEGIN ROADMAP GENERATION NOW:");

        log.debug("Built roadmap prompt with length: {} characters", prompt.length());
        return prompt.toString();
    }

    @Override
    public RoadmapAIResponse parseRoadmapResponse(String rawResponse) {
        try {
            if (rawResponse == null || rawResponse.trim().isEmpty()) {
                log.warn("AI returned null/empty roadmap response. Returning metadata-only fallback response");
                return enrichResponseWithMetadata(null);
            }

            log.debug("Parsing roadmap response, length: {} characters", rawResponse.length());

            // Extract JSON from response
            String jsonString = extractJsonFromResponse(rawResponse);
            log.debug("Extracted JSON length: {} characters", jsonString.length());

            // Parse JSON to RoadmapAIResponse
            RoadmapAIResponse response = objectMapper.readValue(jsonString, RoadmapAIResponse.class);

            // Validate response
            validateRoadmapResponse(response);

            // Add metadata if missing
            response = enrichResponseWithMetadata(response);

            log.info("Successfully parsed roadmap response with {} stages",
                    response.stages() != null ? response.stages().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Error parsing roadmap response: {}", e.getMessage(), e);
            String rawPreview = rawResponse == null ? "null"
                    : rawResponse.substring(0, Math.min(500, rawResponse.length()));
            log.error("Raw response preview: {}", rawPreview);
            throw new RuntimeException("Failed to parse roadmap response: " + e.getMessage(), e);
        }
    }

    private String callGeminiAPI(String prompt) {
        try {
            log.info("Calling Gemini API with model: {}", geminiConfig.getModel());
            log.debug("Prompt length: {} characters", prompt.length());

            GenerateContentResponse response = geminiClient.models.generateContent(
                    geminiConfig.getModel(),
                    prompt,
                    null);

            String responseText = response != null ? response.text() : null;
            if (responseText == null || responseText.trim().isEmpty()) {
                log.warn("Gemini API returned null/empty text response");
                return null;
            }

            log.info("Received response from Gemini API, length: {} characters", responseText.length());
            log.debug("Raw response preview: {}", responseText.substring(0, Math.min(200, responseText.length())));

            return responseText;

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String extractJsonFromResponse(String rawResponse) {
        // Remove markdown code blocks
        String cleaned = rawResponse.trim();

        // Remove ```json and ``` markers
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");

        // Find JSON object boundaries
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new RuntimeException("No valid JSON object found in response");
        }

        String jsonString = cleaned.substring(firstBrace, lastBrace + 1);
        log.debug("Extracted clean JSON, length: {} characters", jsonString.length());

        return jsonString;
    }

    private void validateRoadmapResponse(RoadmapAIResponse response) {
        if (response == null) {
            throw new RuntimeException("Parsed response is null");
        }

        if (response.title() == null || response.title().trim().isEmpty()) {
            throw new RuntimeException("Roadmap title is missing");
        }

        if (response.stages() == null || response.stages().isEmpty()) {
            throw new RuntimeException("Roadmap must have at least one stage");
        }

        // Validate each stage
        for (RoadmapAIResponse.StageAIResponse stage : response.stages()) {
            if (stage.stageName() == null || stage.stageName().trim().isEmpty()) {
                throw new RuntimeException("Stage name is missing");
            }

            if (stage.durationWeeks() == null || stage.durationWeeks() <= 0) {
                throw new RuntimeException("Stage duration must be positive: " + stage.stageName());
            }

            if (stage.schedules() == null || stage.schedules().isEmpty()) {
                throw new RuntimeException("Stage must have at least one schedule: " + stage.stageName());
            }

            // Validate each schedule
            for (RoadmapAIResponse.ScheduleAIResponse schedule : stage.schedules()) {
                if (schedule.scheduleName() == null || schedule.scheduleName().trim().isEmpty()) {
                    throw new RuntimeException("Schedule name is missing");
                }

                if (schedule.dayOfWeek() == null || schedule.dayOfWeek().trim().isEmpty()) {
                    throw new RuntimeException("Schedule day of week is missing");
                }

                // Validate day of week format (accept both English and Vietnamese)
                if (!isValidDayOfWeek(schedule.dayOfWeek())) {
                    throw new RuntimeException("Invalid day of week: " + schedule.dayOfWeek());
                }

                if (schedule.exercises() == null || schedule.exercises().isEmpty()) {
                    throw new RuntimeException("Schedule must have at least one exercise: " + schedule.scheduleName());
                }

                // Validate each exercise
                for (RoadmapAIResponse.ExerciseAIResponse exercise : schedule.exercises()) {
                    if (exercise.exerciseName() == null || exercise.exerciseName().trim().isEmpty()) {
                        throw new RuntimeException("Exercise name is missing");
                    }

                    if (exercise.sets() == null) {
                        throw new RuntimeException("Exercise sets is missing: " + exercise.exerciseName());
                    }
                    if (exercise.sets() <= 0) {
                        throw new RuntimeException("Exercise sets must be > 0: " + exercise.exerciseName());
                    }

                    if (exercise.reps() == null) {
                        throw new RuntimeException("Exercise reps is missing: " + exercise.exerciseName());
                    }
                    if (exercise.reps() <= 0) {
                        throw new RuntimeException("Exercise reps must be > 0: " + exercise.exerciseName());
                    }

                    if (exercise.durationSeconds() == null) {
                        throw new RuntimeException("Exercise durationSeconds is missing: " + exercise.exerciseName());
                    }
                    if (exercise.durationSeconds() <= 0) {
                        throw new RuntimeException(
                                "Exercise durationSeconds must be > 0: " + exercise.exerciseName());
                    }

                    if (exercise.restSeconds() == null) {
                        throw new RuntimeException("Exercise restSeconds is missing: " + exercise.exerciseName());
                    }
                    if (exercise.restSeconds() <= 0) {
                        throw new RuntimeException("Exercise restSeconds must be > 0: " + exercise.exerciseName());
                    }
                }
            }

            // Validate supplement recommendations if present
            if (stage.supplementRecommendations() != null) {
                for (var supplement : stage.supplementRecommendations()) {
                    if (supplement.supplementName() == null || supplement.supplementName().trim().isEmpty()) {
                        throw new RuntimeException("Supplement name is missing in stage: " + stage.stageName());
                    }
                    if (supplement.priority() != null) {
                        String priority = supplement.priority().toUpperCase();
                        if (!priority.equals("HIGH") && !priority.equals("MEDIUM") && !priority.equals("LOW")) {
                            log.warn("Invalid supplement priority: {} in stage: {}", supplement.priority(),
                                    stage.stageName());
                        }
                    }
                }
            }
        }

        log.debug("Roadmap response validation passed");
    }

    private RoadmapAIResponse enrichResponseWithMetadata(RoadmapAIResponse response) {
        if (response == null) {
            return new RoadmapAIResponse(
                    "AI response unavailable",
                    "AI did not return roadmap content. Returning metadata fallback response.",
                    Collections.emptyList(),
                    new BigDecimal("0.00"),
                    AIModel.fromModelName(geminiConfig.getModel()),
                    Instant.now(),
                    "Fallback response generated because AI output was null or empty.");
        }

        // Set AI model from config if not present
        AIModel aiModel = response.aiModel();
        if (aiModel == null) {
            aiModel = AIModel.fromModelName(geminiConfig.getModel());
        }

        // Set generation timestamp if not present
        Instant generatedAt = response.generatedAt();
        if (generatedAt == null) {
            generatedAt = Instant.now();
        }

        // Set confidence score if missing
        BigDecimal confidenceScore = response.confidenceScore();
        if (confidenceScore == null) {
            confidenceScore = new BigDecimal("0.85"); // Default confidence
        }

        return new RoadmapAIResponse(
                response.title(),
                response.description(),
                response.stages(),
                confidenceScore,
                aiModel,
                generatedAt,
                response.notes());
    }

    private String getWorkoutLevelDescription(Enum<?> level) {
        if (level == null)
            return "Not specified";
        switch (level.name()) {
            case "BEGINNER":
                return "Beginner (0-1 years experience)";
            case "INTERMEDIATE":
                return "Intermediate (1-3 years experience)";
            case "ADVANCED":
                return "Advanced (3+ years experience)";
            default:
                return level.name();
        }
    }

    private String getWorkoutFrequencyDescription(Enum<?> frequency) {
        if (frequency == null)
            return "Not specified";
        switch (frequency.name()) {
            case "SEDENTARY":
                return "Sedentary (0 days/week)";
            case "LIGHT":
                return "Light (1-2 days/week)";
            case "MODERATE":
                return "Moderate (3-4 days/week)";
            case "ACTIVE":
                return "Active (5-6 days/week)";
            case "ATHLETE":
                return "Athlete (7+ days/week)";
            default:
                return frequency.name();
        }
    }

    private int calculateOptimalStages(int durationWeeks) {
        if (durationWeeks <= 4)
            return 1;
        if (durationWeeks <= 8)
            return 2;
        if (durationWeeks <= 12)
            return 3;
        if (durationWeeks <= 16)
            return 4;
        return (durationWeeks / 4); // 4 weeks per stage for longer programs
    }

    private boolean isValidDayOfWeek(String dayOfWeek) {
        if (dayOfWeek == null)
            return false;

        String normalized = dayOfWeek.trim().toUpperCase();

        // Accept English day names
        try {
            java.time.DayOfWeek.valueOf(normalized);
            return true;
        } catch (IllegalArgumentException e) {
            // Continue to check Vietnamese names
        }

        // Accept Vietnamese day names
        return normalized.equals("THỨ HAI") ||
                normalized.equals("THỨ BA") ||
                normalized.equals("THỨ TƯ") ||
                normalized.equals("THỨ NĂM") ||
                normalized.equals("THỨ SÁU") ||
                normalized.equals("THỨ BẢY") ||
                normalized.equals("CHỦ NHẬT");
    }
}
