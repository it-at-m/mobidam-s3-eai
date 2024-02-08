package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.MobidamException;
import de.muenchen.mobidam.rest.OASError;
import de.muenchen.mobidam.rest.OASErrorErrorsInner;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;

@Component
public class S3OperationWrapper implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

            // Invalid ServletContextPath is handled by servlet container
            var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).replace("/", "");
            var bucketName = isBucketName(exchange);

            if (bucketName != null) {

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
                        // No OASError because invalid ServletContextPath is handled by servlet container
                        exchange.setException(new MobidamException("REST ContextPath not found : " + contextPath));
                }
        }
    }

    private String isBucketName(Exchange exchange) {
     var bucketName = exchange.getIn().getHeader(Constants.BUCKET_NAME, String.class);
     if (bucketName == null || bucketName.trim().isEmpty()) {

         var bucketNameNotExistsInner = new OASErrorErrorsInner();
         bucketNameNotExistsInner.setErrorCode(String.valueOf(HttpStatus.SC_BAD_REQUEST));
         bucketNameNotExistsInner.message("Bucket name not exists.");
         bucketNameNotExistsInner.path(String.format("%s?%s)", exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class), exchange.getIn().getHeader("CamelHttpQuery", String.class)));

         var bucketNameNotExists = new OASError();
         bucketNameNotExists.setMessage("Bucket name is null or empty.");
         bucketNameNotExists.addErrorsItem(bucketNameNotExistsInner);
         exchange.setProperty(Exchange.EXCEPTION_CAUGHT, bucketNameNotExists);
         return null;
     }
     return bucketName;
    }

}
