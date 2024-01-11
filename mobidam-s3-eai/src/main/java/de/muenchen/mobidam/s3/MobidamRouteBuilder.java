/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MobidamRouteBuilder extends RouteBuilder {

    @Value("${output}")
    private String outputRoute;

    public static final String DIRECT_ROUTE = "direct:eai-route";
    public static final String DIRECT_EXCEPTION = "direct:exception-handler";

    @Override
    public void configure() {
        onException(Exception.class).handled(true).log(LoggingLevel.ERROR, "${exception}");

        from(DIRECT_ROUTE)
                .routeId("eai-route")
                .log(LoggingLevel.DEBUG, "de.muenchen",
                        "Projekt Camel Routing ergaenzen ... (https://camel.apache.org/components/latest/eips/enterprise-integration-patterns.html).")
                .to(outputRoute);
    }

}
