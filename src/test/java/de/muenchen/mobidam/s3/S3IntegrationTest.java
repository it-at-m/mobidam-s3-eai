/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.rest.BucketContentInner;
import org.apache.camel.CamelContext;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

import java.util.ArrayList;
import java.util.Collection;

@Disabled
@CamelSpringBootTest
@SpringBootTest(classes = { Application.class }, properties = { "camel.springboot.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder," })
@ActiveProfiles("integration")
class S3IntegrationTest {

    @Produce()
    private ProducerTemplate producer;

    @Value("${camel.component.aws2-s3.override-endpoint}")
    private String s3url;

    @Autowired
    private CamelContext camelContext;

    private static final String TEST_BUCKET = "test-bucket";

    @Test
    void s3BucketObjectsListTest() {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(TEST_BUCKET).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, "filesInFolder")
                .withHeader("bucketName", TEST_BUCKET).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send("{{camel.route.common}}", exchange);
        var objects = bucketResponse.getIn().getBody(ArrayList.class);
        Assertions.assertEquals("Test.csv", ((BucketContentInner) objects.stream().toList().get(0)).getKey());

    }

    @Test
    void s3PresignedObjectUrlTest() {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(TEST_BUCKET).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, "filesInFolder")
                .withHeader("bucketName", TEST_BUCKET).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send("{{camel.route.common}}", exchange);
        var objectsCollection = bucketResponse.getIn().getBody(Collection.class);

        var object = (BucketContentInner) objectsCollection.iterator().next();

        exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.KEY, object.getKey()).build();

        bucketResponse = producer.send("{{camel.route.presignedUrl}}", exchange);

        var linkCollection = bucketResponse.getIn().getBody(Collection.class);

        var link = (String) linkCollection.iterator().next();
        var compare = String.format("https://%s.%s/Test.csv", TEST_BUCKET, s3url.substring(s3url.indexOf("//") + 2));
        Assertions.assertTrue(link.contains(compare), "Url not found: " + compare);

    }

}
