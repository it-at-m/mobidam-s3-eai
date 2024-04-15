package de.muenchen.mobidam.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class represents the configuration for S3 bucket credentials.
 * It contains a map of S3Credentials objects, with the keys being the names of the S3 buckets.
 */
@Component
@ConfigurationProperties(prefix = "mobidam.s3")
@Getter
@Setter
public class S3BucketCredentialConfig {

    private Map<String, BucketCredentialConfig> bucketCredentialConfig;

    @Getter
    @Setter
    public static class BucketCredentialConfig {

        private String accessKeyEnvVar;
        private String secretKeyEnvVar;
    }
}
