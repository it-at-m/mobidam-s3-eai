package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.config.EnvironmentReader;
import de.muenchen.mobidam.config.S3BucketCredentialConfig;
import de.muenchen.mobidam.exception.MobidamException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class S3CredentialProviderTest {

    private EnvironmentReader environmentReader;
    private S3BucketCredentialConfig properties;
    private S3CredentialProvider s3CredentialProvider;
    private final CamelContext camelContext = new DefaultCamelContext();

    @BeforeEach
    void setup() {
        environmentReader = Mockito.mock(EnvironmentReader.class);
        properties = Mockito.mock(S3BucketCredentialConfig.class);
        s3CredentialProvider = new S3CredentialProvider(properties, environmentReader);
    }

    @Test
    public void test_processWithValidConfiguration() throws Exception {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(Constants.PARAMETER_BUCKET_NAME, bucketName);

        // When
        s3CredentialProvider.process(exchange);

        // Then
        Assertions.assertEquals(value, exchange.getMessage().getHeader(Constants.ACCESS_KEY));
        Assertions.assertEquals(value, exchange.getMessage().getHeader(Constants.SECRET_KEY));
    }

    @Test
    public void test_processWithMissingEnvVars() {
        // Given
        String bucketName = "x-itmkm82k";
        String envVar = "FOO";
        String value = "BAR";
        configureEnvironment(bucketName, envVar, value);
        Mockito.when(environmentReader.getEnvironmentVariable(Mockito.anyString())).thenReturn(null);
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.getMessage().setHeader(Constants.PARAMETER_BUCKET_NAME, bucketName);

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
        exchange.getMessage().setHeader(Constants.PARAMETER_BUCKET_NAME, "invalid_bucket");

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

        // Then
        Assertions.assertThrows(MobidamException.class, () -> {
            // When
            s3CredentialProvider.process(exchange);
        });
    }

    private void configureEnvironment(String bucketName, String envVar, String value) {
        Mockito.when(environmentReader.getEnvironmentVariable(envVar)).thenReturn(value);
        S3BucketCredentialConfig.BucketCredentialConfig envVars = new S3BucketCredentialConfig.BucketCredentialConfig();
        envVars.setAccessKeyEnvVar(envVar);
        envVars.setSecretKeyEnvVar(envVar);
        Map<String, S3BucketCredentialConfig.BucketCredentialConfig> map = new HashMap<>();
        map.put(bucketName, envVars);
        Mockito.when(properties.getBucketCredentialConfig()).thenReturn(map);
    }
}
