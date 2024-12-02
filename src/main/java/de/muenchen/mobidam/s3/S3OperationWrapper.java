package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.domain.MobidamArchive;
import de.muenchen.mobidam.eai.common.CommonConstants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import java.time.Duration;
import java.time.LocalDate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class S3OperationWrapper implements Processor {

    @Value("${mobidam.download.expiration:30}")
    private int downloadExpiration;

    @Value("${mobidam.archive.expiration-months:1}")
    private int archiveExpiration;

    @Override
    public void process(Exchange exchange) throws Exception {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class);
        var bucketName = exchange.getIn().getHeader(CommonConstants.HEADER_BUCKET_NAME, String.class);

        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            exchange.getIn().setHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects);
            exchange.getIn().setHeader(Constants.PARAMETER_ARCHIVED, constructPrefixPattern(exchange));
            break;

        case Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL:

            var objectName = exchange.getIn().getHeader(Constants.PARAMETER_OBJECT_NAME, String.class);
            if (objectName == null) {
                CommonError error = ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST.value(), "Object name is empty");
                exchange.getMessage().setBody(error);
                throw new MobidamException("Object name is empty");
            }

            var archive = exchange.getIn().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class) != null
                    ? exchange.getIn().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class)
                    : false;
            var key = archive ? constructPrefixPattern(exchange) + objectName : objectName;
            var objectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();

            var presignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(downloadExpiration))
                    .getObjectRequest(objectRequest)
                    .build();

            exchange.getIn().setHeader(AWS2S3Constants.KEY, key);
            exchange.getIn().setBody(presignRequest);
            break;

        case Constants.CAMEL_SERVLET_CONTEXT_PATH_ARCHIVE:

            objectName = exchange.getIn().getHeader(Constants.PARAMETER_OBJECT_NAME, String.class);
            if (objectName == null) {
                CommonError error = ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST.value(), "Object name is empty");
                exchange.getMessage().setBody(error);
                throw new MobidamException("Object name is empty");
            }

            exchange.getIn().setHeader(AWS2S3Constants.BUCKET_DESTINATION_NAME, bucketName);
            exchange.getIn().setHeader(AWS2S3Constants.KEY, objectName);
            exchange.getIn().setHeader(AWS2S3Constants.DESTINATION_KEY, Constants.ARCHIVE_PATH + objectName);

            var archiveEntity = new MobidamArchive();
            archiveEntity.setBucket(bucketName);
            archiveEntity.setPath(exchange.getIn().getHeader(AWS2S3Constants.DESTINATION_KEY, String.class));
            archiveEntity.setCreation(LocalDate.now());
            archiveEntity.setExpiration(LocalDate.now().plusMonths(archiveExpiration));
            exchange.setProperty(Constants.ARCHIVE_ENTITY, archiveEntity);

            break;

        default:
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.NOT_FOUND.value(), "REST ContextPath not found : " + contextPath));
            throw new MobidamException("REST ContextPath not found : " + contextPath);
        }
    }

    private String constructPrefixPattern(Exchange exchange) {

        var archive = exchange.getIn().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class) != null
                ? exchange.getIn().getHeader(Constants.PARAMETER_ARCHIVED, Boolean.class)
                : false;
        var path = exchange.getIn().getHeader(Constants.PARAMETER_PATH, String.class);

        var prefixPattern = path != null ? path.endsWith(Constants.DELIMITER) ? path : path + Constants.DELIMITER : "";
        if (archive)
            if (exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).equals(Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL))
                prefixPattern = Constants.ARCHIVE_PATH + prefixPattern;
            else
                prefixPattern = Constants.S3_PREFIX + Constants.ARCHIVE_PATH + prefixPattern;
        else
            prefixPattern = Constants.S3_PREFIX + prefixPattern;

        return prefixPattern;

    }

}
