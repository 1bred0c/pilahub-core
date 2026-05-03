package fpt.edu.sep490.pilahub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.api.version:1.0.0}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define JWT Security Scheme
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Pilahub API Documentation")
                        .description("""
                                ## Pilahub Backend REST API
                                
                                Comprehensive API documentation for Pilahub application.
                                
                                ### Features:
                                - 🔐 **Authentication**: JWT-based authentication with email OTP verification
                                - 👤 **Account Management**: User registration, login, and profile management
                                - 📧 **Email Service**: OTP verification and notifications
                                - 🔒 **Security**: BCrypt password hashing, secure token management
                                
                                ### Getting Started:
                                1. **Register** a new account via `/api/auth/register`
                                2. **Verify** your email with OTP via `/api/auth/verify-email`
                                3. **Login** to get JWT token via `/api/auth/login`
                                4. Use the **Authorize** button above and enter: `Bearer <your-token>`
                                5. Access protected endpoints with your token
                                
                                ### Response Format:
                                All endpoints return standardized `ApiResponse<T>` format:
                                ```json
                                {
                                  "success": true/false,
                                  "message": "Descriptive message",
                                  "data": { /* response payload */ },
                                  "errorCode": "ERROR_CODE",
                                  "timestamp": 1737676800000
                                }
                                ```
                                """)
                        .version(apiVersion)
                        .contact(new Contact()
                                .name("Pilahub Development Team")
                                .url("https://github.com/pilahub"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.pilahub.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login endpoint")));
    }

}