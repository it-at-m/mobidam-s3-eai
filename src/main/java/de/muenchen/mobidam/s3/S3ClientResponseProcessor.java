package de.muenchen.mobidam.s3;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class S3ClientResponseProcessor implements Processor {

    @Produce(value = S3RouteBuilder.S3Client)
    ProducerTemplate s3Client;

    @Override
    public void process(Exchange exchange) {
        s3Client.send(exchange);
    }
}
