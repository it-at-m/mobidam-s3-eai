/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;


import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Collection;

/**
 * Der Test dient der Demonstration wie mit einer beliebigen Testkonfiguration
 * (test/application.yml) die
 * gesamte EAI vom Start bis zum Shutdown mit einem Testaufruf getestet werden kann.
 */
@Disabled
@SpringBootTest
@CamelSpringBootTest
class S3IntegrationTest {

    @Produce(MobidamRouteBuilder.COMMON_S3_OPERATIONS)
    private ProducerTemplate producer;

    @Value("${camel.component.aws2-s3.bucket}")
    private String bucket;

    @Autowired
    private CamelContext camelContext;

    @Test
    void s3ListBucketObjectsTest() throws InterruptedException {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(bucket).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send(exchange);
        var objects = bucketResponse.getIn().getBody(Collection.class);

        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("Test.csv", ((S3Object)objects.stream().toList().get(0)).key());

    }

    @Test
    void s3PresignedObjectUrlTest() throws InterruptedException {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(bucket).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send(exchange);
        var objectsCollection = bucketResponse.getIn().getBody(Collection.class);

        Assertions.assertEquals(1, objectsCollection.size());
        S3Object object = (S3Object) objectsCollection.iterator().next();

        exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.KEY, object.key()).build();

        bucketResponse = producer.send(MobidamRouteBuilder.CREATELINK_S3_OPERATION, exchange);

        var linkCollection = bucketResponse.getIn().getBody(Collection.class);
        Assertions.assertEquals(1, linkCollection.size());
        var link = (String)linkCollection.iterator().next();
        Assertions.assertTrue(link.contains("https://int-mdasc-mdasdev.s3k.muenchen.de/Test.csv"));


    }

    @Test
    void s3ListBucketsTest() throws InterruptedException {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(bucket).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listBuckets).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send(exchange);
        var objects = bucketResponse.getIn().getBody(Collection.class);

        Assertions.assertEquals(1, objects.size());
        Assertions.assertEquals("int-mdasc-mdasdev", ((Bucket)objects.stream().toList().get(0)).name());

    }

}
