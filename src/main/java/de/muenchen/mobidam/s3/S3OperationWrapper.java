package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.MobidamException;
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
            var contextPath = exchange.getIn().getHeader("CamelServletContextPath", String.class).replace("/", "");
            var bucketName = isBucketName(exchange);

            if (bucketName != null) {

                switch (contextPath) {
                    case "filesInFolder":
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
     var bucketName = exchange.getIn().getHeader("bucketName", String.class);
     if (bucketName == null || bucketName.trim().isEmpty()) {

         var bucketNameNotExistsInner = new OASErrorErrorsInner();
         bucketNameNotExistsInner.setErrorCode(String.valueOf(HttpStatus.SC_BAD_REQUEST));
         bucketNameNotExistsInner.message("Bucket name not exists.");
         bucketNameNotExistsInner.path(String.format("%s?%s)", exchange.getIn().getHeader("CamelServletContextPath", String.class), exchange.getIn().getHeader("CamelHttpQuery", String.class)));

         var bucketNameNotExists = new OASError();
         bucketNameNotExists.setMessage("Bucket name not found.");
         bucketNameNotExists.addErrorsItem(bucketNameNotExistsInner);
         exchange.setProperty(Exchange.EXCEPTION_CAUGHT, bucketNameNotExists);
         return null;
     }
     return bucketName;
    }

}
