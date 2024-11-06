/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.rest;

import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.TestConstants;
import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@CamelSpringBootTest
@SpringBootTest(
        classes = { Application.class }, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = { "camel.main.java-routes-include-pattern=**/S3Api" }
)
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = { "camel.route.common=mock:common" })
@ActiveProfiles(TestConstants.SPRING_NO_SECURITY_PROFILE)
class S3ApiPresignedUrlTest {

    @Produce("http:127.0.0.1:8081/api")
    private ProducerTemplate producer;

    @EndpointInject("mock:common")
    private MockEndpoint commonRoute;

    @Test
    public void test_RouteWithBucketNameHeaderEmpty() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/presignedUrl?bucketName=", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("", exchange.getMessage().getHeader(CommonConstants.HEADER_BUCKET_NAME));
        Assertions.assertEquals("/presignedUrl", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithBucketNameHeaderExist() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/presignedUrl?bucketName=TEST", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(CommonConstants.HEADER_BUCKET_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PARAMETER_ARCHIVED));
        Assertions.assertEquals("/presignedUrl", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithArchiveHeaderExist() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/presignedUrl?bucketName=TEST&archived=true", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(CommonConstants.HEADER_BUCKET_NAME));
        Assertions.assertTrue(exchange.getMessage().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class));
        Assertions.assertEquals("/presignedUrl", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithArchiveAndPathHeaderExist() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/presignedUrl?bucketName=sub1/sub2/TEST&archived=true", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("sub1/sub2/TEST", exchange.getMessage().getHeader(CommonConstants.HEADER_BUCKET_NAME));
        Assertions.assertTrue(exchange.getMessage().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class));
        Assertions.assertEquals("/presignedUrl", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

}
