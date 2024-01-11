/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2021
 */
package de.muenchen.mobidam.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Der Test dient der Demonstration wie mit einer beliebigen Testkonfiguration
 * (test/application.yml) die
 * gesamte EAI vom Start bis zum Shutdown mit einem Testaufruf getestet werden kann.
 */
@SpringBootTest
@CamelSpringBootTest
class EaiTest {

    @Produce(MobidamRouteBuilder.DIRECT_ROUTE)
    private ProducerTemplate producer;

    @EndpointInject("mock:output")
    private MockEndpoint output;

    @Test
    void sendToMockTest() throws InterruptedException {

        var message = "Hello Test !";

        output.expectedMessageCount(1);

        producer.sendBody(message);

        output.assertIsSatisfied();
        assertEquals(message, output.getExchanges().get(0).getMessage().getBody(String.class));

    }

}
