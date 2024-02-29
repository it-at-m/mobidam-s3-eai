package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.exception.MobidamException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;
import org.apache.camel.component.aws2.s3.AWS2S3Operations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class S3OperationWrapper implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).replace("/", "");

        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            exchange.getIn().setHeader(AWS2S3Constants.S3_OPERATION, AWS2S3Operations.listObjects);
            break;
        default:
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.NOT_FOUND.value(), "REST ContextPath not found : " + contextPath));
            throw new MobidamException("REST ContextPath not found : " + contextPath);
        }
    }

}
