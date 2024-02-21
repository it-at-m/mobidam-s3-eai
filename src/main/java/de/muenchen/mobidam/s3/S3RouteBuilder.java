/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.exception.ExceptionRouteBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3RouteBuilder extends RouteBuilder {

    public static final String OPERATION_COMMON = "direct:commonOperations";
    public static final String OPERATION_CREATE_LINK = "direct:createLink";
    public static final String S3Client = "direct:s3client";

    @Override
    public void configure() {

        errorHandler(deadLetterChannel(ExceptionRouteBuilder.EXCEPTION_HANDLING).useOriginalMessage());

        onException(S3Exception.class)
                .handled(true)
                .process(exchange -> {
                    var s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, S3Exception.class);
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(s3Exception.statusCode(), s3Exception.getLocalizedMessage()));
                });

        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    var exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(400, exception.getLocalizedMessage()));
                });

        from(OPERATION_COMMON)
                .routeId("S3-Operation-Common").routeDescription("S3 Operation Handling")
                .log(LoggingLevel.DEBUG, Constants.MOBIDAM_LOGGER, "Message received ${header.CamelHttpUrl}")
                .process("s3OperationWrapper")
                .toD(String.format("aws2-s3://{{camel.component.aws2-s3.bucket}}?S3Client=#s3Client&operation=${header.%s}&pojoRequest=true",
                        AWS2S3Constants.S3_OPERATION))
                .process("restResponseWrapper");

        from(OPERATION_CREATE_LINK)
                .routeId("S3-Operation-CreateLink").routeDescription("Execute S3 Create Link Operation")
                .toD("aws2-s3://{{camel.component.aws2-s3.bucket}}?accessKey={{camel.component.aws2-s3.access-key}}&secretKey={{camel.component.aws2-s3.secret-key}}&region={{camel.component.aws2-s3.region}}&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation="
                        + AWS2S3Operations.createDownloadLink);

    }

}
