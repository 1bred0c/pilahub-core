package fpt.edu.sep490.pilahub.config;

import fpt.edu.sep490.pilahub.config.properties.CorsProperties;
import fpt.edu.sep490.pilahub.config.properties.GhnProperties;
import fpt.edu.sep490.pilahub.config.properties.MoMoProperties;
import fpt.edu.sep490.pilahub.config.properties.VNPayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        CorsProperties.class,
        VNPayProperties.class,
        MoMoProperties.class,
        GhnProperties.class
})
public class PropertiesConfig {
}
