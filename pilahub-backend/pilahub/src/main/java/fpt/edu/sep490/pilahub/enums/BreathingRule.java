package fpt.edu.sep490.pilahub.enums;

public enum BreathingRule {
    // Standard patterns
    INHALE_ON_EFFORT,       // Inhale during the exertion phase
    EXHALE_ON_EFFORT,       // Exhale during the exertion phase (most common in strength training)
    NASAL_BREATHING,        // Breathe through the nose only
    MOUTH_BREATHING,        // Breathe through the mouth

    // Advanced patterns
    BOX_BREATHING,          // Equal inhale, hold, exhale, hold (4-4-4-4 or similar)
    DIAPHRAGMATIC,          // Deep belly/diaphragm breathing
    RHYTHMIC,               // Coordinated rhythmic pattern matched to movement tempo

    // Special cases
    HOLD_BREATH,            // Brief breath hold during peak effort (e.g. Valsalva)
    FREE_BREATHING          // No specific pattern required
}
