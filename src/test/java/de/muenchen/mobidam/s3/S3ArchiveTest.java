/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.repository.ArchiveRepository;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = { "camel.springboot.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder," }
)
@TestPropertySource(
        properties = {
                "FOO_ACCESS_KEY=foo",
                "FOO_SECRET_KEY=bar"
        }
)
@EnableAutoConfiguration
@DirtiesContext
class S3ArchiveTest {

    @Produce()
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    @Autowired()
    private ArchiveRepository archiveRepository;

    private static LocalS3 localS3;

    private static S3Client s3InitClient;

    private static final String TEST_BUCKET = "test-bucket";

    private static final String OBJECT_KEY = "File_1.csv";

    @Value("${mobidam.archive.name:archive}")
    private String archive;

    @Value("${mobidam.archive.delimiter:/}")
    private String delimiter;

    @Value("${mobidam.archive.expiration-month:/}")
    private int expiration;

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
    public void test_RouteWithArchiveTest() {

        // Set S3 test-bucket content
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key(OBJECT_KEY).build(),
                Path.of(new File("src/test/resources/s3/Test.csv").toURI()));

        var s3Request = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_ARCHIVE)
                .withHeader(Constants.BUCKET_NAME, TEST_BUCKET)
                .withHeader(Constants.OBJECT_NAME, OBJECT_KEY)
                .build();
        var response = producer.send("{{camel.route.common}}", s3Request);

        var bucketContent = s3InitClient.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());

        Assertions.assertEquals(1, bucketContent.contents().size());
        Assertions.assertEquals(archive + delimiter + OBJECT_KEY, bucketContent.contents().get(0).key());

        var dbContent = archiveRepository.findAll();
        Assertions.assertEquals(1, dbContent.size());
        Assertions.assertEquals(archive + delimiter + OBJECT_KEY, dbContent.get(0).getPath());
        Assertions.assertEquals(TEST_BUCKET, dbContent.get(0).getBucket());
        Assertions.assertEquals(LocalDate.now(), dbContent.get(0).getDate());
        Assertions.assertEquals(LocalDate.now().plusMonths(expiration), dbContent.get(0).getExpiration());

    }

}
