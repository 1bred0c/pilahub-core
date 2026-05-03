package pilahub.enums;

public enum AIModel {
    GEMINI_2_0_FLASH_EXP("gemini-2.0-flash-exp"),
    GEMINI_2_5_FLASH_PREVIEW("gemini-2.5-flash-preview"),
    GEMINI_3_FLASH_PREVIEW("gemini-3-flash-preview"),
    GEMINI_PRO("gemini-pro");

    private final String modelName;

    AIModel(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public static AIModel fromModelName(String modelName) {
        for (AIModel model : values()) {
            if (model.modelName.equalsIgnoreCase(modelName)) {
                return model;
            }
        }
        // Default fallback
        return GEMINI_3_FLASH_PREVIEW;
    }
}
