package de.muenchen.mobidam;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExceptionRouteBuilder extends RouteBuilder {

    public static final String EXCEPTION_HANDLING = "seda:exceptionHandling";
    public static final String MOBIDAM_LOGGER = "de.muenchen.mobidam";

    @Override
    public void configure() throws Exception {

        from(EXCEPTION_HANDLING).routeId("EXCEPTION_HANDLER").to(String.format("log:%s?showAll=true&multiline=true", MOBIDAM_LOGGER));

    }

}
