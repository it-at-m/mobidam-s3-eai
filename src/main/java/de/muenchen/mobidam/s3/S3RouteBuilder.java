/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.ExceptionRouteBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3RouteBuilder extends RouteBuilder {

    public static final String OPERATION_COMMON = "direct:commonOperations";
    public static final String OPERATION_CREATE_LINK = "direct:createLink";


    @Override
    public void configure() {

        errorHandler(deadLetterChannel(ExceptionRouteBuilder.DIRECT_EXCEPTION_HANDLING).useOriginalMessage());

        from(OPERATION_COMMON)
                .routeId("S3-Operation-Common").routeDescription("Execute S3 Operation")
                .log(LoggingLevel.DEBUG, ExceptionRouteBuilder.MOBIDAM_LOGGER, "Message received ... ")
                .process(new S3OperationWrapper())
                .toD(String.format("aws2-s3://{{camel.component.aws2-s3.bucket}}?S3Client=#s3Client&operation=${header.%s}&pojoRequest=true", AWS2S3Constants.S3_OPERATION))
                .process(new RestResponseWrapper());

        from(OPERATION_CREATE_LINK)
                .routeId("S3-Operation-CreateLink").routeDescription("Execute S3 Create Link Operation")
                .toD("aws2-s3://{{camel.component.aws2-s3.bucket}}?accessKey={{camel.component.aws2-s3.access-key}}&secretKey={{camel.component.aws2-s3.secret-key}}&region={{camel.component.aws2-s3.region}}&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=" + AWS2S3Operations.createDownloadLink);

    }

}
