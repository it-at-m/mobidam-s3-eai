/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import de.muenchen.mobidam.Application;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.http.common.HttpMethods;
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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

@CamelSpringBootTest
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"camel.springboot.java-routes-include-pattern=**/OpenapiRESTRouteBuilder,**/S3RouteBuilder,**/ExceptionRouteBuilder,"})
@EnableAutoConfiguration
@DirtiesContext
class S3Test {

    @Produce("http:127.0.0.1:8081/filesInFolder")
    private ProducerTemplate producer;

    @Value("${camel.component.aws2-s3.bucket}")
    private String bucket;

    @Autowired
    private CamelContext camelContext;

    private static LocalS3 localS3;

    @Autowired
    private S3Client s3Client;

    private static S3Client s3InitClient;

    // Same as camel.component.aws2-s3.bucket
    private static final String TEST_BUCKET = "test-bucket";

    @BeforeAll
    public static void setUp() throws URISyntaxException {

        localS3 = LocalS3.builder()
                .port(8080)
                .mode(LocalS3Mode.PERSISTENCE)
                .dataPath("download")
                .build();

        localS3.start();

        s3InitClient = S3Client.builder().endpointOverride(new URI("http://127.0.0.1:8080")).region(Region.of("local")).credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "foo"))).build();

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
    public void listObjectTest() {

        // Set S3 test-bucket content
        s3InitClient.putObject(PutObjectRequest.builder().bucket(TEST_BUCKET).key("File_1.csv").build(), Path.of(new File("s3/Test.csv").toURI()));

        var openapiRequest = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
                .withHeader(Exchange.HTTP_URI, "http://127.0.0.1:8081/api/filesInFolder?bucketName=" + bucket)
                .build();
        var response = producer.send(openapiRequest);
        var json = response.getOut().getBody(String.class);
        // [class FilesInFolderGet200ResponseInner {
        //    key: File_1.csv
        //    lastmodified: ...
        //    size: 15
        //}]
        Assertions.assertTrue(json.contains("File_1.csv"));

    }

    @Test
    public void bucketNameNotFoundTest()  {

        var openapiRequest = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
                .withHeader(Exchange.HTTP_URI, "http://127.0.0.1:8081/api/filesInFolder?bucketName=foo")
                .build();
        var response = producer.send(openapiRequest);
        var json = response.getOut().getBody(String.class);
//        class OASError {
//            message: S3 Client error.
//                    errors: [class OASErrorErrorsInner {
//                path: /filesInFolder?bucketName=foo)
//                message: Bucket 'foo' not exist. (Service: S3, Status Code: 404, Request ID: 7153702453966274560)
//                errorCode: 404
//            }]
//        }
        Assertions.assertTrue(json.contains("Bucket 'foo' not exist"));
    }

    @Test
    public void bucketNameNullOrEmptyTest()  {

        var openapiRequest = ExchangeBuilder.anExchange(camelContext)
                .withHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
                .withHeader(Exchange.HTTP_URI, "http://127.0.0.1:8081/api/filesInFolder?bucketName=")
                .build();
        var response = producer.send(openapiRequest);
        var json = response.getOut().getBody(String.class);
//        class OASError {
//            message: Bucket name is null or empty.
//            errors: [class OASErrorErrorsInner {
//                path: /filesInFolder?bucketName=)
//                message: Bucket name not exists.
//                errorCode: 400
//            }]
//        }
        Assertions.assertTrue(json.contains("Bucket name not exists."));


    }

}
