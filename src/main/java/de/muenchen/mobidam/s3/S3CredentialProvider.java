package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.common.EnvironmentReader;
import de.muenchen.mobidam.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.exception.MobidamException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.tooling.model.Strings;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This class provides the credentials for S3 buckets.
 * It takes the configured environment variables from the properties, reads their content
 * and provides them as message headers.
 */
@Component
@RequiredArgsConstructor
public class S3CredentialProvider implements Processor {

    private final S3BucketCredentialConfig properties;
    private final EnvironmentReader environmentReader;

    @Override
    public void process(Exchange exchange) throws Exception {
        String bucketName = verifyBucket(exchange);
        S3BucketCredentialConfig.S3Credentials credentials = verifyCredentials(bucketName);
        String accessKey = environmentReader.getEnvironmentVariable(credentials.getAccessKeyEnvVar());
        String secretKey = environmentReader.getEnvironmentVariable(credentials.getSecretKeyEnvVar());
        if (Strings.isNullOrEmpty(accessKey) || Strings.isNullOrEmpty(secretKey)) {
            throw new MobidamException("Credentials for bucket " + bucketName + " not configured");
        }
        exchange.getMessage().setHeader(Constants.ACCESS_KEY, accessKey);
        exchange.getMessage().setHeader(Constants.SECRET_KEY, secretKey);
    }

    private String verifyBucket(Exchange exchange) throws MobidamException {
        String bucketName = exchange.getMessage().getHeader(Constants.BUCKET_NAME, String.class);
        if (bucketName == null) {
            throw new MobidamException("Bucket name is missing");
        }
        return bucketName;
    }

    private S3BucketCredentialConfig.S3Credentials verifyCredentials(String bucketName) throws MobidamException {
        Map<String, S3BucketCredentialConfig.S3Credentials> map = properties.getS3BucketCredentials();
        S3BucketCredentialConfig.S3Credentials envVars = map.get(bucketName);
        if (envVars == null) {
            throw new MobidamException("Configuration for bucket " + bucketName + " not found");
        }
        return envVars;
    }
}
