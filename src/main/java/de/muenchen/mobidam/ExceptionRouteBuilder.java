package de.muenchen.mobidam;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class ExceptionRouteBuilder extends RouteBuilder {

    public static final String DIRECT_EXCEPTION_HANDLING = "direct:exceptionHandling";
    public static final String MOBIDAM_LOGGER = "de.muenchen";

    @Override
    public void configure() throws Exception {

        from(DIRECT_EXCEPTION_HANDLING).routeId("EXCEPTION_HANDLER").to("log:de.muenchen?showAll=true&multiline=true");

    }

}
