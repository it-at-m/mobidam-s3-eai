/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;


import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
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
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.Collection;

@Disabled
@CamelSpringBootTest
@SpringBootTest(classes = { Application.class }, properties = {"camel.springboot.java-routes-include-pattern=**/S3RouteBuilder,**/ExceptionRouteBuilder," })
@ActiveProfiles("integration")
class S3IntegrationTest {

    @Produce(S3RouteBuilder.OPERATION_COMMON)
    private ProducerTemplate producer;

    @Value("${camel.component.aws2-s3.bucket}")
    private String bucket;

    @Value("${camel.component.aws2-s3.override-endpoint}")
    private String s3url;

    @Autowired
    private CamelContext camelContext;

    @Test
    void s3BucketObjectsListTest() {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(bucket).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, "filesInFolder").withHeader("bucketName", bucket).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send(exchange);
        var objects = bucketResponse.getIn().getBody(ArrayList.class);
        Assertions.assertEquals("Test.csv", ((S3Object)objects.stream().toList().get(0)).key());

    }

    @Test
    void s3PresignedObjectUrlTest() {

        var s3RequestObjects = ListObjectsRequest.builder().bucket(bucket).build();
        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, "filesInFolder").withHeader("bucketName", bucket).build();
        exchange.getIn().setBody(s3RequestObjects);
        var bucketResponse = producer.send(exchange);
        var objectsCollection = bucketResponse.getIn().getBody(Collection.class);

        S3Object object = (S3Object) objectsCollection.iterator().next();

        exchange = ExchangeBuilder.anExchange(camelContext).withHeader(AWS2S3Constants.KEY, object.key()).build();

        bucketResponse = producer.send(S3RouteBuilder.OPERATION_CREATE_LINK, exchange);

        var linkCollection = bucketResponse.getIn().getBody(Collection.class);

        var link = (String)linkCollection.iterator().next();
        var compare = String.format("https://%s.%s/Test.csv", bucket, s3url.substring(s3url.indexOf("//") + 2));
        Assertions.assertTrue(link.contains(compare), "Url not found: " + compare);


    }


}
