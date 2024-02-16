package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.rest.BucketContent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import de.muenchen.mobidam.rest.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@Slf4j
public class RestResponseWrapper implements Processor {

    @Value("${mobidam.limit.search.items:20}")
    private int maxS3ObjectItems;

    @Override
    public void process(Exchange exchange) throws Exception {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class).replace("/", "");
        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            filesInFile(exchange);
            break;
        default:
            ErrorResponse res = ErrorResponseBuilder.build(404, "REST ContextPath not found : " + contextPath);
            exchange.getMessage().setBody(res);
        }

    }

    private void filesInFile(Exchange exchange) {

        var objects = exchange.getIn().getBody(Collection.class);

        if (objects.size() > maxS3ObjectItems) {
            log.warn("More than {} objects in storage", maxS3ObjectItems);
        }

        var files = new ArrayList<BucketContent>();

        objects.forEach(s3object -> {
            var file = new BucketContent();
            file.setKey(((S3Object) s3object).key());
            file.setLastmodified(((S3Object) s3object).lastModified().toString());
            file.setSize(new BigDecimal(((S3Object) s3object).size()));
            files.add(file);

        });
        exchange.getMessage().setBody(files);
    }

}
