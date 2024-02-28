package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.exception.MobidamException;
import de.muenchen.mobidam.rest.ErrorResponse;
import java.time.Duration;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.apache.camel.tooling.model.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
public class S3OperationWrapper implements Processor {

    @Value("${mobidam.limit.download:30}")
    private int downloadLimit; // The URL will expire in downloadLimit minutes.

    @Override
    public void process(Exchange exchange) throws Exception {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class);
        var bucketName = exchange.getIn().getHeader(Constants.BUCKET_NAME, String.class);

        if (Strings.isNullOrEmpty(bucketName)) {
            ErrorResponse res = ErrorResponseBuilder.build(400, "Bucket name is empty");
            exchange.getMessage().setBody(res);
            throw new MobidamException("Bucket name is empty");
        }

        var prefix = exchange.getIn().getHeader(Constants.PATH_ALIAS_PREFIX, String.class);

        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:

            if (prefix != null)
                exchange.getIn().setBody(ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).build());
            else
                exchange.getIn().setBody(ListObjectsRequest.builder().bucket(bucketName).build());

            exchange.getIn().setHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects);
            break;

        case Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL:

            var objectKey = exchange.getIn().getHeader(Constants.OBJECT_NAME, String.class);

            if (Strings.isNullOrEmpty(bucketName)) {
                ErrorResponse res = ErrorResponseBuilder.build(400, "Bucket name is empty");
                exchange.getMessage().setBody(res);
                throw new MobidamException("Bucket name is empty");
            }

            if (objectKey == null)
                break;

            var key = prefix != null ? prefix + objectKey : objectKey;
            var objectRequest = GetObjectRequest.builder().bucket(bucketName).key(key).build();

            var presignRequest = GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(downloadLimit))
                    .getObjectRequest(objectRequest)
                    .build();

            exchange.getIn().setHeader(AWS2S3Constants.KEY, key);
            exchange.getIn().setBody(presignRequest);

            break;

        default:
            ErrorResponse res = ErrorResponseBuilder.build(404, "REST ContextPath not found : " + contextPath);
            exchange.getMessage().setBody(res);
            throw new MobidamException("REST ContextPath not found : " + contextPath);
        }
    }

}
