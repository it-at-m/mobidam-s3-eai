/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.TestConstants;
import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "camel.main.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder,"
        }
)
@ExtendWith(SystemStubsExtension.class)
@EnableAutoConfiguration
@DirtiesContext
@ActiveProfiles(TestConstants.SPRING_NO_SECURITY_PROFILE)
class S3BucketTest {

    @Produce
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    private static LocalS3 localS3;

    private static S3Client s3InitClient;

    private static final String TEST_BUCKET = "test-bucket";

    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("FOO_ACCESS_KEY", "foo", "FOO_SECRET_KEY", "bar");

    @BeforeAll
    public static void setUp() throws URISyntaxException {

        localS3 = LocalS3.builder()
                .port(8080)
                .mode(LocalS3Mode.PERSISTENCE)
                .dataPath("src/test/resources/s3/")
                .build();

        localS3.start();

        s3InitClient = S3Client.builder().endpointOverride(new URI("http://127.0.0.1:8080")).region(Region.of("local"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "foo"))).build();

        // Remove old test content
        var bucketsInTest = s3InitClient.listBuckets();

        // Remove bucket objects
        bucketsInTest.buckets().forEach(b -> {
            var content = s3InitClient.listObjects(ListObjectsRequest.builder().bucket(b.name()).build());
            content.contents().forEach(o -> {
                s3InitClient.deleteObject(DeleteObjectRequest.builder().bucket(b.name()).key(o.key()).build());
            });
        });
        // Delete buckets
        bucketsInTest.buckets().forEach(b -> {
            s3InitClient.deleteBucket(DeleteBucketRequest.builder().bucket(b.name()).build());
        });

        // Create test bucket
        s3InitClient.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
    }

    @AfterAll
    public static void shutdown() {
        localS3.shutdown();
    }

    @Test
    public void test_RouteWithBucketNameParameterNotExist() {

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER)
                .build();
        var response = producer.send("{{camel.route.common}}", s3Request);

        var error = response.getIn().getBody(CommonError.class);
        Assertions.assertEquals("Bucket name is missing", error.getError());
        Assertions.assertEquals(BigDecimal.valueOf(HttpStatus.BAD_REQUEST.value()), error.getStatus());
    }

    @Test
    public void test_RouteWithBucketNameNotFoundAndTenantNotFound() {

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER)
                .withHeader(CommonConstants.HEADER_BUCKET_NAME, "foo")
                .build();
        var response = producer.send("{{camel.route.common}}", s3Request);

        var error = response.getIn().getBody(CommonError.class);
        Assertions.assertEquals("Bucket not configured: foo", error.getError());
        Assertions.assertEquals(BigDecimal.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), error.getStatus());

    }

    @Test
    public void test_RouteWithBucketNameNull() {

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER)
                .withHeader(CommonConstants.HEADER_BUCKET_NAME, null)
                .build();
        var response = producer.send("{{camel.route.common}}", s3Request);

        var error = response.getIn().getBody(CommonError.class);
        Assertions.assertEquals("Bucket name is missing", error.getError());
        Assertions.assertEquals(BigDecimal.valueOf(HttpStatus.BAD_REQUEST.value()), error.getStatus());
    }

    @Test
    public void test_RouteWithBucketNameEmpty() {

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER)
                .withHeader(CommonConstants.HEADER_BUCKET_NAME, "")
                .build();
        var response = producer.send("{{camel.route.common}}", s3Request);

        var error = response.getIn().getBody(CommonError.class);
        Assertions.assertEquals("Bucket name is missing", error.getError());
        Assertions.assertEquals(BigDecimal.valueOf(HttpStatus.BAD_REQUEST.value()), error.getStatus());
    }

}
