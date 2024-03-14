/**
* NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech) (6.6.0).
* https://openapi-generator.tech
* Do not edit the class manually.
*/
package de.muenchen.mobidam.rest;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.stereotype.Component;

@Component
public class S3Api extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        /**
         * GET /filesInFolder : Get S3 bucket object list
         **/
        rest()
                .get("/filesInFolder")
                .description("Get S3 bucket object list")
                .id("filesInFolderGetApi")
                .produces("application/json")
                .outType(BucketContentInner[].class)
                .param()
                .name("bucketName")
                .type(RestParamType.query)
                .required(true)
                .description("Bucket name")
                .endParam()
                .param()
                .name("archive")
                .type(RestParamType.query)
                .required(false)
                .description("S3 path")
                .endParam()
                .to("{{camel.route.common}}");

        /**
         * GET /presignedUrl : Retrieve download link
         **/
        rest()
                .get("/presignedUrl")
                .description("Retrieve download link")
                .id("viewObjectDownloadLink")
                .produces("application/json")
                .outType(PresignedUrl.class)
                .param()
                .name("bucketName")
                .type(RestParamType.query)
                .required(true)
                .description("Bucket name")
                .endParam()
                .param()
                .name("objectName")
                .type(RestParamType.query)
                .required(true)
                .description("Object name")
                .endParam()
                .to("{{camel.route.common}}");

        /**
         * PUT /archive : Move &#39;finshed&#39; file to archive.
         **/
        rest()
                .put("/archive")
                .description("Move 'finished' file to archive.")
                .id("moveFinishedFileToArchiveApi")
                .produces("application/json")
                .outType(Void.class)
                .param()
                .name("bucketName")
                .type(RestParamType.query)
                .required(true)
                .description("Bucket name")
                .endParam()
                .param()
                .name("objectName")
                .type(RestParamType.query)
                .required(true)
                .description("Object name")
                .endParam()
                .to("{{camel.route.common}}");

    }
}
