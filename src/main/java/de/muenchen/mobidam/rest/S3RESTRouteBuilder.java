/**
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.6.0).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package de.muenchen.mobidam.rest;

import de.muenchen.mobidam.exception.ExceptionRouteBuilder;
import de.muenchen.mobidam.s3.S3RouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
public class S3RESTRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        errorHandler(deadLetterChannel(ExceptionRouteBuilder.EXCEPTION_HANDLING).useOriginalMessage());

        /**
         * GET /filesInFolder
         **/
        rest()
                .get("/filesInFolder")
                .description("")
                .produces("application/json")
                .outType(BucketContent[].class)
                .param()
                .name("bucketName")
                .type(RestParamType.query)
                .required(true)
                .description("Bucket name")
                .endParam()
                .param()
                .name("path")
                .type(RestParamType.query)
                .required(false)
                .description("S3 path")
                .endParam()
                .to(S3RouteBuilder.OPERATION_COMMON);

    }
}
