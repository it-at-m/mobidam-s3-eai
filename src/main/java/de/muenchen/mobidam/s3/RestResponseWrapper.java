package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.rest.BucketContent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class RestResponseWrapper implements Processor {

    @Value("${mobidam.limit.search.items:20}")
    private int maxS3ObjectItems;

    @Override
    public void process(Exchange exchange) throws Exception {

        try {

            if (exchange.getIn().getBody() instanceof ResponseEntity<?>) {
                return;
            }

            var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).replace("/", "");
            switch (contextPath) {
            case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
                filesInFile(exchange);
                break;
            default:
                exchange.getOut().setBody(new ResponseEntity<>("REST ContextPath not found : " + contextPath, HttpStatusCode.valueOf(400)));
            }
        } catch (Exception exception) {
            exchange.getOut().setBody(new ResponseEntity<>(exception.getLocalizedMessage(), HttpStatusCode.valueOf(500)));
            exchange.setException(null);
        }

    }

    private void filesInFile(Exchange exchange) {

        var objects = exchange.getIn().getBody(Collection.class);

        if (objects.size() > maxS3ObjectItems) {
            var responseException = new ResponseEntity<>(String.format("Request supply limit %s is exceeded.", maxS3ObjectItems), HttpStatusCode.valueOf(400));
            exchange.getOut().setBody(responseException);
            exchange.setException(null);
            return;
        }

        var files = new ArrayList<BucketContent>();

        objects.forEach(s3object -> {
            var file = new BucketContent();
            file.setKey(((S3Object) s3object).key());
            file.setLastmodified(((S3Object) s3object).lastModified().toString());
            file.setSize(new BigDecimal(((S3Object) s3object).size()));
            files.add(file);

        });
        var response = new ResponseEntity<>(files, HttpStatusCode.valueOf(200));
        exchange.getOut().setBody(response);
    }

}
