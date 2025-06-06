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
import de.muenchen.mobidam.eai.common.config.EnvironmentReader;
import de.muenchen.mobidam.rest.BucketContentInner;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = { "camel.main.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder," } // In the test only start included routes
)
@TestPropertySource(
        properties = {
                "mobidam.limit.search.items=1",
        }
)
@EnableAutoConfiguration
@DirtiesContext
@ActiveProfiles(TestConstants.SPRING_NO_SECURITY_PROFILE)
class S3FileLimitTest {

    @Produce
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    @MockBean
    private EnvironmentReader environmentReader;

    private static LocalS3 localS3;

    private static S3Client s3InitClient;

    private static final String TEST_BUCKET = "test-bucket";

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
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key("File_1.csv").build(),
                Path.of(new File("src/test/resources/s3/Test.csv").toURI()));
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key("File_2.csv").build(),
                Path.of(new File("src/test/resources/s3/Test.csv").toURI()));

    }

    @AfterAll
    public static void shutdown() {
        localS3.shutdown();
    }

    @Test
    public void test_RouteWithExceedFileLimit() {

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER)
                .withHeader(CommonConstants.HEADER_BUCKET_NAME, TEST_BUCKET)
                .build();

        Mockito.when(environmentReader.getEnvironmentVariable("FOO_ACCESS_KEY")).thenReturn("foo");
        Mockito.when(environmentReader.getEnvironmentVariable("FOO_SECRET_KEY")).thenReturn("bar");

        var response = producer.send("{{camel.route.common}}", s3Request);

        List<BucketContentInner> files = response.getIn().getBody(List.class);

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("File_1.csv", files.get(0).getKey());

    }

}
