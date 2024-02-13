/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
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
    public static final String S3Client = "direct:s3client";

    @Override
    public void configure() {

        from(OPERATION_COMMON)
                .routeId("S3-Operation-Common").routeDescription("S3 Operation Handling")
                .log(LoggingLevel.DEBUG, Constants.MOBIDAM_LOGGER, "Message received ${header.CamelHttpUrl}")
                .process("s3OperationWrapper")
                .process("s3ClientErrorWrapper")
                .process("restResponseWrapper");

        from(S3Client).routeId("S3-Request").routeDescription("Execute S3 Operation")
             .toD(String.format("aws2-s3://{{camel.component.aws2-s3.bucket}}?S3Client=#s3Client&operation=${header.%s}&pojoRequest=true", AWS2S3Constants.S3_OPERATION));

        from(OPERATION_CREATE_LINK)
                .routeId("S3-Operation-CreateLink").routeDescription("Execute S3 Create Link Operation")
                .toD("aws2-s3://{{camel.component.aws2-s3.bucket}}?accessKey={{camel.component.aws2-s3.access-key}}&secretKey={{camel.component.aws2-s3.secret-key}}&region={{camel.component.aws2-s3.region}}&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=" + AWS2S3Operations.createDownloadLink);

    }

}
