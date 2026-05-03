package fpt.edu.sep490.pilahub.config;

import fpt.edu.sep490.pilahub.config.properties.CorsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket configuration.
 *
 * <p><b>Client connection:</b>
 * <pre>
 *   const socket = new SockJS('/ws/notifications');
 *   const stompClient = Stomp.over(socket);
 *   stompClient.connect(
 *       { Authorization: 'Bearer &lt;your-jwt-token&gt;' },
 *       () => {
 *           // Subscribe to personal notification channel
 *           stompClient.subscribe('/user/queue/notifications', (msg) => {
 *               const notification = JSON.parse(msg.body);
 *               console.log('New notification:', notification);
 *           });
 *       }
 *   );
 * </pre>
 *
 * <p>The server pushes new notifications to:
 * <code>/user/{accountId}/queue/notifications</code>
 * WebSocket configuration for real-time heart rate streaming
 * Enables STOMP protocol over WebSocket for 1-1 communication between Trainee and Coach
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final CorsProperties corsProperties;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for sending messages to clients
        // /topic is for 1-to-many broadcasting (not used in our case)
        // /queue is for 1-to-1 messaging (we use this for trainee -> coach)
        config.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages FROM clients
        config.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific destinations
        config.setUserDestinationPrefix("/user");

        log.info("WebSocket message broker configured successfully");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")  // Tighten this in production
                .withSockJS();

        // Register main STOMP endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .withSockJS();

        // Register heart rate specific endpoint (allows separate connection management)
        registry.addEndpoint("/ws/heartrate")
                .setAllowedOrigins(corsProperties.getAllowedOrigins().toArray(new String[0]))
                .withSockJS();

        log.info("WebSocket STOMP endpoints registered: /ws, /ws/heartrate");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add authentication interceptor to validate JWT tokens
        registration.interceptors(webSocketAuthInterceptor);
        registration.interceptors(new WebSocketLoggingInterceptor());
        log.info("✅ WebSocket logging interceptor registered");
    }
}

