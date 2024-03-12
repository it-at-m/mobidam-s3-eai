/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.rest;

import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = { "camel.springboot.java-routes-include-pattern=**/S3Api" }
)
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "camel.route.common=mock:common" })
class S3ApiFilesInFolderTest {

    @Produce("http:127.0.0.1:8081/api")
    private ProducerTemplate producer;

    @EndpointInject("mock:common")
    private MockEndpoint commonRoute;

    @Test
    public void test_RouteWithBucketNameHeaderNotExistTest() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/filesInFolder", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.BUCKET_NAME));
        Assertions.assertEquals("/filesInFolder", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithBucketNameHeaderEmptyTest() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/filesInFolder?bucketName=", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("", exchange.getMessage().getHeader(Constants.BUCKET_NAME));
        Assertions.assertEquals("/filesInFolder", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithBucketNameHeaderExistTest() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/filesInFolder?bucketName=TEST", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(Constants.BUCKET_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PATH_ALIAS_PREFIX));
        Assertions.assertEquals("/filesInFolder", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithPathHeaderExistTest() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/filesInFolder?bucketName=TEST&path=FOO", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(Constants.BUCKET_NAME));
        Assertions.assertEquals("FOO", exchange.getMessage().getHeader(Constants.PATH_ALIAS_PREFIX));
        Assertions.assertEquals("/filesInFolder", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

}
