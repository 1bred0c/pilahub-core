package fpt.edu.sep490.pilahub.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

public class WebSocketLoggingInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command != null) {
            switch (command) {
                case CONNECT:
                    System.out.println("🟢 [WS CONNECTED] User bắt đầu kết nối - Session ID: " + accessor.getSessionId());
                    break;
                case DISCONNECT:
                    System.out.println("🔴 [WS DISCONNECTED] User chủ động ngắt kết nối - Session ID: " + accessor.getSessionId());
                    break;
                case SUBSCRIBE:
                    System.out.println("🔔 [WS SUBSCRIBED] Session ID: " + accessor.getSessionId() + " theo dõi channel: " + accessor.getDestination());
                    break;
                case SEND:
                    System.out.println("📩 [WS SEND] Session ID: " + accessor.getSessionId() + " gửi tin đến: " + accessor.getDestination());
                    break;
                default:
                    break;
            }
        } else {
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // Có thể comment lại nếu không muốn console bị spam quá nhiều
        // System.out.println("📡 [WS SENT] " + message);
    }
}