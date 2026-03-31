package org.ganjp.api.cms.logo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "logo.upload")
@Data
public class LogoUploadProperties {
    private String directory = "uploads/logos";
}
