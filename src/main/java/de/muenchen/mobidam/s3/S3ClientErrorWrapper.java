package de.muenchen.mobidam.s3;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
public class S3ClientErrorWrapper implements Processor {

    @Produce(value = S3RouteBuilder.S3Client)
    ProducerTemplate s3Client;

    @Override
    public void process(Exchange exchange) throws Exception {

        var response = s3Client.send(exchange);

        if (response.getException() != null) {
            var exception = response.getException(Exception.class);
            exchange.getOut().setBody(new ResponseEntity<>(exception.getLocalizedMessage(),  exception instanceof S3Exception ? HttpStatusCode.valueOf(((S3Exception)exception).statusCode()) : HttpStatusCode.valueOf(400)));
            exchange.setException(null);
        }

    }
}
