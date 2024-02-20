package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.config.S3BucketCredentialConfig;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**

 This class provides the credentials for S3 buckets.

 It takes the configured environment variables from the properties, reads their content
 and provides them as message headers.
 */
@Component
@RequiredArgsConstructor
public class S3CredentialProvider implements Processor {

    private final S3BucketCredentialConfig properties;

    @Override
    public void process(Exchange exchange) {
        String bucketName = exchange.getMessage().getHeader(Constants.BUCKET_NAME, String.class);
        Map<String, S3BucketCredentialConfig.S3Credentials> map = properties.getS3BucketCredentials();
        S3BucketCredentialConfig.S3Credentials envVar = map.get(bucketName);
        String accessKey = System.getenv(envVar.getAccessKeyEnvVar());
        String secretKey = System.getenv(envVar.getSecretKeyEnvVar());
        exchange.getMessage().setHeader(Constants.ACCESS_KEY, accessKey);
        exchange.getMessage().setHeader(Constants.SECRET_KEY, secretKey);
    }
}
