/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.rest;

import de.muenchen.mobidam.Application;
import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.TestConstants;
import de.muenchen.mobidam.eai.common.S3Constants;
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
class S3ApiArchiveTest {

    @Produce("http:127.0.0.1:8081/api")
    private ProducerTemplate producer;

    @EndpointInject("mock:common")
    private MockEndpoint commonRoute;

    @Test
    public void test_RouteWithBucketNameAndObjectNameHeaderNotExist() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/archive?httpMethod=PUT", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertNull(exchange.getMessage().getHeader(S3Constants.PARAMETER_BUCKET_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PARAMETER_OBJECT_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PARAMETER_PATH));
        Assertions.assertEquals("/archive", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithBucketNameAndObjectNameHeaderEmpty() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/archive?bucketName=&objectName=&httpMethod=PUT", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("", exchange.getMessage().getHeader(S3Constants.PARAMETER_BUCKET_NAME));
        Assertions.assertEquals("", exchange.getMessage().getHeader(Constants.PARAMETER_OBJECT_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PARAMETER_PATH));
        Assertions.assertEquals("/archive", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

    @Test
    public void test_RouteWithBucketNameAndObjectNameHeaderExist() throws InterruptedException {

        commonRoute.expectedMessageCount(1);
        producer.sendBody("http:127.0.0.1:8081/api/archive?bucketName=TEST&objectName=TEST&httpMethod=PUT", null);
        commonRoute.assertIsSatisfied();

        var exchange = commonRoute.getExchanges().get(0);
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(S3Constants.PARAMETER_BUCKET_NAME));
        Assertions.assertEquals("TEST", exchange.getMessage().getHeader(Constants.PARAMETER_OBJECT_NAME));
        Assertions.assertNull(exchange.getMessage().getHeader(Constants.PARAMETER_PATH));
        Assertions.assertEquals("/archive", exchange.getMessage().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH));
    }

}
