package fpt.edu.sep490.pilahub.config;

import fpt.edu.sep490.pilahub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Intercepts WebSocket messages to authenticate users via JWT token
 * Stores authentication in session attributes for persistence across messages
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String AUTH_ATTR = "SPRING_SECURITY_CONTEXT";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.debug("WebSocket command: {}", accessor.getCommand());

            // Handle CONNECT command - authenticate and store in session
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                Authentication auth = authenticateUser(accessor);
                if (auth != null) {
                    // Store in session attributes
                    if (accessor.getSessionAttributes() != null) {
                        accessor.getSessionAttributes().put(AUTH_ATTR, auth);
                        accessor.setUser(auth);
                        log.info("Stored authentication in session attributes for user: {}", auth.getName());
                    } else {
                        log.error("Session attributes is NULL! Cannot store authentication");
                    }
                }
            }
            // Handle other commands - restore authentication from session
            else {
                log.debug("Processing non-CONNECT command, trying to restore auth...");

                // Try to get from user first (Spring stores it here)
                if (accessor.getUser() instanceof Authentication) {
                    Authentication auth = (Authentication) accessor.getUser();
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("✅ Restored authentication from User for: {}", auth.getName());
                    return message;
                }

                // Try to get from session attributes
                if (accessor.getSessionAttributes() != null) {
                    Authentication auth = (Authentication) accessor.getSessionAttributes().get(AUTH_ATTR);
                    if (auth != null) {
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("✅ Restored authentication from session attributes for: {}", auth.getName());
                    } else {
                        log.warn("❌ Session attributes exists but no authentication found. Keys: {}",
                                accessor.getSessionAttributes().keySet());
                    }
                } else {
                    log.error("❌ Session attributes is NULL for non-CONNECT message!");
                }
            }
        } else {
            log.warn("StompHeaderAccessor is null");
        }

        return message;
    }

    private Authentication authenticateUser(StompHeaderAccessor accessor) {
        // Extract JWT token from Authorization header
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Extract username from token
                String username = jwtUtil.extractUsername(token);

                if (username != null) {
                    // Load user details and validate token
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails)) {
                        // Create authentication
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.info("WebSocket authenticated user: {}", username);
                        return auth;
                    } else {
                        log.warn("Invalid JWT token for WebSocket connection");
                    }
                }
            } catch (Exception e) {
                log.error("Error authenticating WebSocket connection: {}", e.getMessage());
            }
        } else {
            log.warn("WebSocket connection attempt without JWT token");
        }

        return null;
    }
}





