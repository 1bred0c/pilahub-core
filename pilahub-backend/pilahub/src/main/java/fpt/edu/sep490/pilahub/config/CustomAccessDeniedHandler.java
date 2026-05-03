package fpt.edu.sep490.pilahub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.error("=== ACCESS DENIED (403) ===");
        log.error("Request URI: {} {}", request.getMethod(), request.getRequestURI());
        log.error("Remote Address: {}", request.getRemoteAddr());

        if (auth != null) {
            log.error("User: {}", auth.getName());
            log.error("Authorities: {}", auth.getAuthorities());
            log.error("Authentication Type: {}", auth.getClass().getSimpleName());
            log.error("Is Authenticated: {}", auth.isAuthenticated());
        } else {
            log.error("No Authentication found in SecurityContext!");
        }

        log.error("Access Denied Reason: {}", accessDeniedException.getMessage(), accessDeniedException);
        log.error("========================");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message = "Access Denied: You don't have permission to access this resource.";
        if (auth != null) {
            message += " Current role(s): " + auth.getAuthorities();
        } else {
            message += " No authentication found.";
        }

        APIResponse<Void> apiResponse = APIResponse.error(message);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

