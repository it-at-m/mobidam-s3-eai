/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import org.apache.camel.*;
import org.apache.camel.builder.ExchangeBuilder;
import org.apache.camel.component.minio.MinioConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Der Test dient der Demonstration wie mit einer beliebigen Testkonfiguration
 * (test/application.yml) die
 * gesamte EAI vom Start bis zum Shutdown mit einem Testaufruf getestet werden kann.
 */
@SpringBootTest
@CamelSpringBootTest
class S3IntegrationTest {

    @Produce(MobidamRouteBuilder.DIRECT_BUCKET)
    private ProducerTemplate producer;

    @EndpointInject("mock:output")
    private MockEndpoint output;

    @Autowired
    CamelContext camelContext;

    @Autowired
    MinioClient minioClient;

    @Test
    void s3ListTest() throws InterruptedException {

        var minioRequestObjects = ListObjectsArgs.builder().bucket("int-mdasc-mdasdev").region("us-east-1").recursive(true);
        var list = minioClient.listObjects(minioRequestObjects.build());

        var exchange = ExchangeBuilder.anExchange(camelContext).withHeader(MinioConstants.OBJECT_NAME, "Test_EXP_VESPA-STAMMSATZ_W01_20230525.csv").build();
//        exchange.getIn().setHeader(MinioConstants.BUCKET_NAME, "int-parkraummanagementk-migration");
//         exchange.getIn().setBody(new ListObjectsArgs());
//
//        exchange.getIn().setHeader(MinioConstants.BUCKET_NAME, "int-parkraummanagementk-migration");

        https://chat.openai.com/c/5aa2602a-60e4-4e3c-8b52-0823ec8b8e68
        
        exchange.getIn().setBody(minioRequestObjects);
        exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");

        var bucketListObjects = producer.send(exchange);

        Assertions.assertNotNull(bucketListObjects);


    }

}
