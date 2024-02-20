package de.muenchen.mobidam.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This class represents the configuration for S3 bucket credentials.
 * It contains a map of S3Credentials objects, with the keys being the names of the S3 buckets.
 */
@Component
@ConfigurationProperties(prefix = "mobidam")
@Getter
@Setter
public class S3BucketCredentialConfig {

    private Map<String, S3Credentials> s3BucketCredentials;

    @Getter
    @Setter
    public static class S3Credentials {

        private String accessKeyEnvVar;
        private String secretKeyEnvVar;
    }
}
