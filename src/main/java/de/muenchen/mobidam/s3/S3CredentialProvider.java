package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.config.EnvironmentReader;
import de.muenchen.mobidam.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.exception.MobidamException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.tooling.model.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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
        S3BucketCredentialConfig.BucketCredentialConfig credentials = verifyCredentials(bucketName, exchange);
        String accessKey = environmentReader.getEnvironmentVariable(credentials.getAccessKeyEnvVar());
        String secretKey = environmentReader.getEnvironmentVariable(credentials.getSecretKeyEnvVar());
        if (Strings.isNullOrEmpty(accessKey) || Strings.isNullOrEmpty(secretKey)) {
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Bucket not configured: " + bucketName));
            throw new MobidamException("Bucket not configured: " + bucketName);
        }
        exchange.getMessage().setHeader(Constants.ACCESS_KEY, accessKey);
        exchange.getMessage().setHeader(Constants.SECRET_KEY, secretKey);
    }

    private String verifyBucket(Exchange exchange) throws MobidamException {
        String bucketName = exchange.getMessage().getHeader(Constants.PARAMETER_BUCKET_NAME, String.class);
        if (Strings.isNullOrEmpty(bucketName)) {
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST.value(), "Bucket name is missing"));
            throw new MobidamException("Bucket name is missing");
        }
        return bucketName;
    }

    private S3BucketCredentialConfig.BucketCredentialConfig verifyCredentials(String bucketName, Exchange exchange) throws MobidamException {
        Map<String, S3BucketCredentialConfig.BucketCredentialConfig> map = properties.getBucketCredentialConfig();
        S3BucketCredentialConfig.BucketCredentialConfig envVars = map.get(bucketName);
        if (envVars == null) {
            exchange.getMessage()
                    .setBody(ErrorResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Configuration for bucket not found: " + bucketName));
            throw new MobidamException("Configuration for bucket not found: " + bucketName);
        }
        return envVars;
    }
}
