package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.eai.common.S3Constants;
import de.muenchen.mobidam.eai.common.config.EnvironmentReader;
import de.muenchen.mobidam.eai.common.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import de.muenchen.mobidam.eai.common.s3.S3CredentialProvider;
import de.muenchen.mobidam.rest.ErrorResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class S3CredentialProviderTest {

    private S3BucketCredentialConfig properties;
    private S3CredentialProvider s3CredentialProvider;
    private final CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    void setup() {
        properties = Mockito.mock(S3BucketCredentialConfig.class);
        s3CredentialProvider = new S3CredentialProvider(properties); //, environmentReader);
    }

    @Test
    public void test_processWithValidConfiguration() throws Exception {

        try(MockedStatic<EnvironmentReader> environmentReader = Mockito.mockStatic(EnvironmentReader.class)) {

        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(S3Constants.PARAMETER_BUCKET_NAME, bucketName);
        exchange.setProperty(S3Constants.ERROR_RESPONSE, new ErrorResponse());

       environmentReader.when(() -> EnvironmentReader.getEnvironmentVariable(envVar)).thenReturn(value);

        // When
        s3CredentialProvider.process(exchange);

        // Then
        Assertions.assertEquals(value, exchange.getMessage().getHeader(S3Constants.ACCESS_KEY));
        Assertions.assertEquals(value, exchange.getMessage().getHeader(S3Constants.SECRET_KEY));
        }
    }

    @Test
    public void test_processWithMissingEnvVars() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(S3Constants.PARAMETER_BUCKET_NAME, bucketName);
        exchange.setProperty(S3Constants.ERROR_RESPONSE, new ErrorResponse());

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    @Test
    public void test_processWithMissingBucketConfiguration() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(S3Constants.PARAMETER_BUCKET_NAME, "invalid_bucket");
        exchange.setProperty(S3Constants.ERROR_RESPONSE, new ErrorResponse());

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    @Test
    public void test_processWithMissingBucketName() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty(S3Constants.ERROR_RESPONSE, new ErrorResponse());

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    private void configureEnvironment(String bucketName, String envVar, String value) {
        S3BucketCredentialConfig.BucketCredentialConfig envVars = new S3BucketCredentialConfig.BucketCredentialConfig();
        envVars.setAccessKeyEnvVar(envVar);
        envVars.setSecretKeyEnvVar(envVar);
        Map<String, S3BucketCredentialConfig.BucketCredentialConfig> map = new HashMap<>();
        map.put(bucketName, envVars);
        Mockito.when(properties.getBucketCredentialConfigs()).thenReturn(map);
    }
}
