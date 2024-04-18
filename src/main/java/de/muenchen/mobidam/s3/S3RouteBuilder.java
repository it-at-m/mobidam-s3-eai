/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.domain.MobidamArchive;
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
import org.springframework.http.HttpStatus;
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
                    if (exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH).equals(Constants.ARCHIVE_ENTITY)) {
                        log.error(String.format("Archive clean up (%s)", exchange.getProperty(Constants.ARCHIVE_ENTITY, MobidamArchive.class).toString()),
                                s3Exception);
                    } else {
                        log.error("Error occurred in route", s3Exception);
                    }
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(s3Exception.statusCode(), s3Exception.getClass().getName()));
                });

        onException(ResolveEndpointFailedException.class)
                .handled(true)
                .process(exchange -> {
                    var s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, ResolveEndpointFailedException.class);
                    if (exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH).equals(Constants.ARCHIVE_ENTITY)) {
                        log.error(String.format("Archive clean up (%s)", exchange.getProperty(Constants.ARCHIVE_ENTITY, MobidamArchive.class).toString()),
                                s3Exception);
                    } else {
                        log.error("Error occurred in route", s3Exception);
                    }
                    exchange.getMessage().setBody(ErrorResponseBuilder.build(400, s3Exception.getClass().getName()));
                });

        onException(Exception.class)
                .handled(true)
                .process(exchange -> {
                    if (exchange.getMessage().getBody()instanceof ErrorResponse res) {
                        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, res.getStatus());
                        if (exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH).equals(Constants.ARCHIVE_ENTITY)) {
                            log.error(
                                    String.format("Archive clean up (%s): %s", exchange.getProperty(Constants.ARCHIVE_ENTITY, MobidamArchive.class).toString(),
                                            res.getError()));
                        }
                    } else {
                        Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
                        if (exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH).equals(Constants.ARCHIVE_ENTITY)) {
                            log.error(String.format("Archive clean up (%s)", exchange.getProperty(Constants.ARCHIVE_ENTITY, MobidamArchive.class).toString()),
                                    exception);
                        } else {
                            log.error("Error occurred in route", exception);
                        }
                        ErrorResponse res = ErrorResponseBuilder.build(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getClass().getName());
                        exchange.getMessage().setBody(res);
                    }
                });

        from("{{camel.route.common}}")
                .routeId("S3-Operation-Common").routeDescription("S3 Operation Handling")
                .log(LoggingLevel.DEBUG, Constants.MOBIDAM_LOGGER, "Message received ${header.CamelHttpUrl}")
                .process("s3OperationWrapper")
                .log(String.format("CamelServletContextPath: ${header.%s}", Constants.CAMEL_SERVLET_CONTEXT_PATH))
                .process("s3CredentialProvider")
                .choice()
                /*
                 * ServletContext path : /filesInFolder
                 */
                .when()
                .simple(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER))
                .toD(String.format(
                        "aws2-s3://${header.%1$s}?accessKey=${header.%2$s}&secretKey=${header.%3$s}&region={{camel.component.aws2-s3.region}}&operation=${header.%4$s}&overrideEndpoint=true&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&${header.%5$s}",
                        Constants.PARAMETER_BUCKET_NAME, Constants.ACCESS_KEY, Constants.SECRET_KEY, AWS2S3Constants.S3_OPERATION,
                        Constants.PARAMETER_ARCHIVED))
                /*
                 * ServletContext path : /presignedUrl
                 */
                .when().simple(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL))
                .toD(String.format(
                        "aws2-s3://${header.%1$s}?accessKey=${header.%2$s}&secretKey=${header.%3$s}&region={{camel.component.aws2-s3.region}}&overrideEndpoint=true&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=%4$s",
                        Constants.PARAMETER_BUCKET_NAME, Constants.ACCESS_KEY, Constants.SECRET_KEY, AWS2S3Operations.createDownloadLink))
                /*
                 * ServletContext path : /archive
                 */
                .when().simple(String.format("${header.%s} == '%s'", Constants.CAMEL_SERVLET_CONTEXT_PATH, Constants.CAMEL_SERVLET_CONTEXT_PATH_ARCHIVE))
                .toD(String.format(
                        "aws2-s3://${header.%1$s}?accessKey=${header.%2$s}&secretKey=${header.%3$s}&region={{camel.component.aws2-s3.region}}&operation=%4$s&overrideEndpoint=true&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}",
                        Constants.PARAMETER_BUCKET_NAME, Constants.ACCESS_KEY, Constants.SECRET_KEY, AWS2S3Operations.copyObject))
                .toD(String.format(
                        "aws2-s3://${header.%1$s}?accessKey=${header.%2$s}&secretKey=${header.%3$s}&region={{camel.component.aws2-s3.region}}&operation=%4$s&overrideEndpoint=true&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}",
                        Constants.PARAMETER_BUCKET_NAME, Constants.ACCESS_KEY, Constants.SECRET_KEY, AWS2S3Operations.deleteObject))
                .setBody(simple(String.format("${exchangeProperty.%s}", Constants.ARCHIVE_ENTITY)))
                .to("bean:archiveService?method=save")
                /*
                 * ServletContext path not found
                 */
                .otherwise()
                .throwException(new MobidamException("REST ContextPath not found."))
                .end()
                .process("restResponseWrapper");

        from("{{camel.route.delete-archive}}")
                .routeId("Clean-up-archive").routeDescription("S3 Archive Clean Up")
                .setHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, simple(Constants.ARCHIVE_ENTITY))
                .to("bean:archiveService?method=expired")
                .log(LoggingLevel.INFO, Constants.MOBIDAM_LOGGER, "Archive clean up started (${body.size} Item(s) found for processing) ...")
                .split(body())
                .setProperty(Constants.ARCHIVE_ENTITY, simple("${body}"))
                .setHeader(Constants.PARAMETER_BUCKET_NAME, simple("${body.bucket}"))
                .process("s3CredentialProvider")
                .setHeader(AWS2S3Constants.KEY, simple("${body.path}"))
                .toD(String.format(
                        "aws2-s3://${header.%1$s}?accessKey=${header.%2$s}&secretKey=${header.%3$s}&region={{camel.component.aws2-s3.region}}&overrideEndpoint=true&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=deleteObject",
                        Constants.PARAMETER_BUCKET_NAME, Constants.ACCESS_KEY, Constants.SECRET_KEY))
                .setBody(simple(String.format("${exchangeProperty.%s}", Constants.ARCHIVE_ENTITY)))
                .to("bean:archiveService?method=delete");

    }
}
