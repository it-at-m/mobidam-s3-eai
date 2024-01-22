/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2023
 */
package de.muenchen.mobidam.s3;

import io.minio.ListObjectsArgs;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.minio.MinioOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MobidamRouteBuilder extends RouteBuilder {

    @Value("${output}")
    private String outputRoute;

    public static final String DIRECT_BUCKET = "direct:bucket";
    public static final String DIRECT_EXCEPTION = "direct:exception-handler";

    @Override
    public void configure() {
        onException(Exception.class).handled(true).log(LoggingLevel.ERROR, "${exception}");

        from(DIRECT_BUCKET)
                .routeId("S3-Bucket")
                .log(LoggingLevel.DEBUG, "de.muenchen","Bucket Abfrage ist gestartet ...")

                // https://stackoverflow.com/questions/76585152/apache-camel-minio-component-listobjects-operation-not-working
                .to("minio://int-mdasc-mdasdev?minioClient=#minioClient&operation=listObjects&pojoRequest=true");
//                  .to("minio:int-mdasc-mdasdev?minioClient=#minioClient&operation=" + MinioOperations.listObjects);


//                .to("minio:int-mdasc-mdasdev?minioClient=#minioClient&pojoRequest=true&operation=" + MinioOperations.listBuckets);
//                .to("minio:int-mdasc-mdasdev?minioClient=#minioClient&operation=" + MinioOperations.listBuckets);
//                .to("minio:int-mdasc-mdasdev?minioClient=#minioClient&operation=getObject")
//                .to("file:download?fileName=${header.CamelMinioObjectName}");


    }

}
