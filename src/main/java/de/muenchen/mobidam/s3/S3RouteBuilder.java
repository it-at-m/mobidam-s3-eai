/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.exception.ExceptionRouteBuilder;
import de.muenchen.mobidam.exception.MobidamException;
import de.muenchen.mobidam.rest.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3RouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        errorHandler(deadLetterChannel(ExceptionRouteBuilder.EXCEPTION_HANDLING).useOriginalMessage());

        onException(S3Exception.class)
                .handled(true)
                .process(exchange -> {
                    var s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, S3Exception.class);
                    log.error("Error occurred in route", s3Exception);
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(s3Exception.statusCode(), s3Exception.getClass().getName()));
                });

        onException(ResolveEndpointFailedException.class)
                .handled(true)
                .process(exchange -> {
                    var s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ResolveEndpointFailedException.class);
                    log.error("Error occurred in route", s3Exception);
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(400, s3Exception.getClass().getName()));
                });

        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    if (exchange.getMessage().getBody()instanceof ErrorResponse res) {
                        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, res.getStatus());
                    } else {
                        Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                        log.error("Error occurred in route", exception);
                        ErrorResponse res = ErrorResponseBuilder.build(500, exception.getClass().getName());
                        exchange.getMessage().setBody(res);
                    }
                });

        from("{{camel.route.common}}")
                .routeId("S3-Operation-Common").routeDescription("S3 Operation Handling")
                .log(LoggingLevel.DEBUG, Constants.MOBIDAM_LOGGER, "Message received ${header.CamelHttpUrl}")
                .process("s3OperationWrapper")
                .log(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER))
                .choice()
                    .when().simple(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER))
                        .toD(String.format("aws2-s3://${header.%2$s}?S3Client=#s3Client&operation=${header.%1$s}&pojoRequest=true", AWS2S3Constants.S3_OPERATION, Constants.BUCKET_NAME))
                    .when().simple(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL))
                        .toD(String.format("aws2-s3://${header.%s}?accessKey={{camel.component.aws2-s3.access-key}}&secretKey={{camel.component.aws2-s3.secret-key}}&region={{camel.component.aws2-s3.region}}&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=%s", Constants.BUCKET_NAME, AWS2S3Operations.createDownloadLink))
                    .otherwise()
                      .throwException(new MobidamException("REST ContextPath not found."))
                .end()
                .process("restResponseWrapper");

    }

}
