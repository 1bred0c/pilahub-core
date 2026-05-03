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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * STOMP channel interceptor that validates the JWT token on each CONNECT frame.
 *
 * <p>Clients must send the token in the STOMP CONNECT headers:
 * <pre>
 *   stompClient.connect({ Authorization: 'Bearer &lt;token&gt;' }, onConnected);
 * </pre>
 *
 * <p>After successful authentication the STOMP session principal is set to the
 * user's email address, which is used by {@link org.springframework.messaging.simp.SimpMessagingTemplate}
 * to route messages to the correct user destination.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WebSocket CONNECT rejected: missing or malformed Authorization header");
            throw new IllegalArgumentException("Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.validateToken(token, userDetails)) {
                log.warn("WebSocket CONNECT rejected: invalid or expired token for {}", email);
                throw new IllegalArgumentException("Invalid or expired JWT token");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);
            log.debug("WebSocket CONNECT authenticated for user: {}", email);

        } catch (Exception ex) {
            log.warn("WebSocket CONNECT rejected: {}", ex.getMessage());
            throw new IllegalArgumentException("WebSocket authentication failed: " + ex.getMessage());
        }

        return message;
    }
}
