/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import lombok.RequiredArgsConstructor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MobidamRouteBuilder extends RouteBuilder {

    public static final String COMMON_S3_OPERATIONS = "direct:commonS3Operations";
    public static final String CREATELINK_S3_OPERATION = "direct:createLink";
    public static final String DIRECT_EXCEPTION = "direct:exception-handler";

    @Override
    public void configure() {

        onException(Exception.class).handled(true).log(LoggingLevel.ERROR, "${exception}");

        from(COMMON_S3_OPERATIONS)
                .routeId("S3-Common-Operations")
                .toD(String.format("aws2-s3://{{camel.component.aws2-s3.bucket}}?S3Client=#s3Client&operation=${header.%s}&pojoRequest=true", AWS2S3Constants.S3_OPERATION));

        from(CREATELINK_S3_OPERATION)
                .routeId("S3-Operation-CreateLink")
                .toD("aws2-s3://{{camel.component.aws2-s3.bucket}}?accessKey={{camel.component.aws2-s3.access-key}}&secretKey={{camel.component.aws2-s3.secret-key}}&region={{camel.component.aws2-s3.region}}&uriEndpointOverride={{camel.component.aws2-s3.override-endpoint}}&operation=" + AWS2S3Operations.createDownloadLink);

    }

}
