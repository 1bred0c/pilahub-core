package pilahub.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiClientConfig {

    @Autowired
    private GeminiConfig geminiConfig;

    @Bean
    public Client geminiClient() {
        // The Gemini Client expects GOOGLE_API_KEY environment variable
        // Set it programmatically from our configuration
        String apiKey = geminiConfig.getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "API key is not configured. Please set GOOGLE_API_KEY environment variable."
            );
        }

        // Set the environment variable that Client expects
        System.setProperty("GOOGLE_API_KEY", apiKey);

        // Now Client() will pick it up from the system property
        return new Client();
    }
}
