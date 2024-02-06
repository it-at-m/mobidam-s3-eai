package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.rest.OASError;
import de.muenchen.mobidam.rest.OASErrorErrorsInner;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3ClientErrorWrapper implements Processor {

    @Produce(value = S3RouteBuilder.S3Client)
    ProducerTemplate callS3Client;

    @Override
    public void process(Exchange exchange) throws Exception {

        var response = callS3Client.send(exchange);

        if (response.getException() != null) {
            var exception = response.getException(S3Exception.class);

            var wrapperErrorInner = new OASErrorErrorsInner();
            wrapperErrorInner.setErrorCode(String.valueOf(exception.statusCode()));
            wrapperErrorInner.message(exception.getMessage());
            wrapperErrorInner.path(String.format("%s?%s)", exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class), exchange.getIn().getHeader("CamelHttpQuery", String.class)));

            var wrapperError = new OASError();
            wrapperError.setMessage("S3 Client error.");
            wrapperError.addErrorsItem(wrapperErrorInner);
            exchange.getOut().setBody(wrapperError);

            exchange.setException(null);

        }

    }
}
