/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.TestConstants;
import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.repository.ArchiveRepository;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = { "camel.main.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder," }
)
@ExtendWith(SystemStubsExtension.class)
@EnableAutoConfiguration
@DirtiesContext
@ActiveProfiles(TestConstants.SPRING_NO_SECURITY_PROFILE)
class S3ArchiveDeleteTest {

    @Produce
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ArchiveRepository archiveRepository;

    private static LocalS3 localS3;

    private static S3Client s3InitClient;

    private static final String TEST_BUCKET = "test-bucket";

    private static final String OBJECT_KEY = "File_1.csv";

    private static final String PATH = "sub1/sub2/";

    @SystemStub
    private EnvironmentVariables environment = new EnvironmentVariables("FOO_ACCESS_KEY", "foo", "FOO_SECRET_KEY", "bar");

    @Value("${mobidam.archive.name:archive}")
    private String archive;

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

    }

    @BeforeEach
    public void cleanUp() {
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

        // Reset database
        archiveRepository.deleteAll();
    }

    @AfterAll
    public static void shutdown() {
        localS3.shutdown();
    }

    @Test
    public void test_RouteWithArchiveDelete() {

        // Set S3 archive content
        var archiveFile1 = new MobidamArchive();
        archiveFile1.setBucket(TEST_BUCKET);
        archiveFile1.setPath(archive + "/" + PATH + OBJECT_KEY);
        archiveFile1.setCreation(LocalDate.now().minusDays(2));
        archiveFile1.setExpiration(LocalDate.now().minusDays(1));
        archiveRepository.saveAndFlush(archiveFile1);

        // Set S3 test-bucket content
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key(archiveFile1.getPath()).build(),
                Path.of(new File("src/test/resources/s3/Test.csv").toURI()));

        var response = producer.send("{{camel.route.delete-archive}}", ExchangeBuilder.anExchange(camelContext).build());

        var bucketContent = s3InitClient.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
        Assertions.assertEquals(0, bucketContent.contents().size());
        var dbContent = archiveRepository.findAll();
        Assertions.assertEquals(0, dbContent.size());

    }

    @Test()
    public void test_RouteWithArchiveDeleteMobidamException() {

        // Set S3 archive content
        var archiveFile1 = new MobidamArchive();
        archiveFile1.setBucket("NOT_EXISTS");
        archiveFile1.setPath(archive + "/" + PATH + OBJECT_KEY);
        archiveFile1.setCreation(LocalDate.now().minusDays(2));
        archiveFile1.setExpiration(LocalDate.now().minusDays(1));
        archiveRepository.saveAndFlush(archiveFile1);

        // Set S3 test-bucket content
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key(archiveFile1.getPath()).build(),
                Path.of(new File("src/test/resources/s3/Test.csv").toURI()));

        var response = producer.send("{{camel.route.delete-archive}}", ExchangeBuilder.anExchange(camelContext).build());

        var bucketContent = s3InitClient.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
        Assertions.assertEquals(1, bucketContent.contents().size());
        var dbContent = archiveRepository.findAll();
        Assertions.assertEquals(1, dbContent.size());

    }

}
