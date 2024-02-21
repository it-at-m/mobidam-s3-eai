package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.rest.BucketContent;
import de.muenchen.mobidam.rest.OkResponse;
import java.util.ArrayList;
import java.util.Collection;
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
            exchange.getMessage().setBody(ErrorResponseBuilder.build(404, "REST ContextPath not found : " + contextPath));
        }

    }

    private void filesInFile(Exchange exchange) {

        var objects = exchange.getIn().getBody(Collection.class);

        if (objects.size() > maxS3ObjectItems) {
            log.warn("More than {} objects in storage", maxS3ObjectItems);
        }

        var files = new ArrayList<BucketContent>();

        objects.stream().limit(maxS3ObjectItems).forEach(object -> {
            var s3Object = (S3Object) object;
            files.add(new BucketContent(s3Object.key(), s3Object.lastModified().toString(), s3Object.size()));
        });
        exchange.getMessage().setBody(new OkResponse(200, files));
    }

}
