package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

@Component
public class S3OperationWrapper implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).replace("/", "");
        var bucketName = exchange.getIn().getHeader(Constants.BUCKET_NAME, String.class);

        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            var prefix = exchange.getIn().getHeader(Constants.PATH_ALIAS_PREFIX, String.class);
            if (prefix != null)
                exchange.getIn().setBody(ListObjectsRequest.builder().bucket(bucketName).prefix(prefix).build());
            else
                exchange.getIn().setBody(ListObjectsRequest.builder().bucket(bucketName).build());

            exchange.getIn().setHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects);
            break;
        default:
            exchange.getMessage().setBody(ErrorResponseBuilder.build(404, "REST ContextPath not found : " + contextPath));
        }
    }

}
