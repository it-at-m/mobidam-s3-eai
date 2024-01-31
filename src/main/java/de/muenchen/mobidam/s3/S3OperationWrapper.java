package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.MobidamException;
import org.apache.camel.CamelConfiguration;
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

        try {
            var contextPath = exchange.getIn().getHeader("CamelServletContextPath", String.class).replace("/", "");
            switch (contextPath) {
                case "filesInFolder":
                    exchange.getIn().setBody(ListObjectsRequest.builder().bucket(exchange.getIn().getHeader("bucketName", String.class)).build());
                    exchange.getIn().setHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects);
                    break;
                default:
                    exchange.setException(new MobidamException("REST ContextPath not found : " + contextPath));
            }
        } catch (Exception ex) {
            exchange.setException(new MobidamException("S3OperationWrapper failed.", ex));
        }

    }
}
