package fpt.edu.sep490.pilahub.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
@Slf4j
public class GoogleTokenVerifier {

    @Value("${google.client-id.web}")
    private String googleClientIdWeb;

    @Value("${google.client-id.mobile}")
    private String googleClientIdMobile;

    /**
     * Verify Google ID token and extract user information
     * Supports both Web and Mobile client IDs
     *
     * @param idTokenString Google ID token from client
     * @return GoogleUserInfo containing email and other info
     * @throws IllegalArgumentException if token is invalid
     */
    public GoogleUserInfo verifyToken(String idTokenString) {
        try {
            // Create verifier with both Web and Mobile client IDs
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(java.util.Arrays.asList(googleClientIdWeb, googleClientIdMobile))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                log.error("Invalid Google ID token");
                throw new IllegalArgumentException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Extract user information
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String givenName = (String) payload.get("given_name");
            String familyName = (String) payload.get("family_name");

            if (!emailVerified) {
                log.error("Email not verified by Google: {}", email);
                throw new IllegalArgumentException("Email not verified by Google");
            }

            log.info("Successfully verified Google token for email: {}", email);

            return new GoogleUserInfo(email, name, givenName, familyName, pictureUrl, emailVerified);

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to verify Google ID token: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    /**
     * Record to hold Google user information
     */
    public record GoogleUserInfo(
            String email,
            String name,
            String givenName,
            String familyName,
            String pictureUrl,
            Boolean emailVerified
    ) {
    }
}


