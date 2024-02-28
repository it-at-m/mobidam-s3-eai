package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.rest.BucketContentInner;
import de.muenchen.mobidam.rest.PresignedUrl;
import java.math.BigDecimal;
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
    public void process(Exchange exchange) {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class);
        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            filesInFile(exchange);
            break;
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL:
                presignedUrl(exchange);
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

        var files = new ArrayList<BucketContentInner>();

        objects.stream().limit(maxS3ObjectItems).forEach(object -> {
            var s3Object = (S3Object) object;
            BucketContentInner content = new BucketContentInner();
            content.setKey(s3Object.key());
            content.setLastmodified(s3Object.lastModified().toString());
            content.setSize(BigDecimal.valueOf(s3Object.size()));
            files.add(content);
        });
        exchange.getMessage().setBody(files);
    }

    private void presignedUrl(Exchange exchange) {

        var urls = new ArrayList<PresignedUrl>();
        var links = exchange.getIn().getBody(Collection.class);

        links.forEach(link -> {
            var file = new PresignedUrl();
            file.setUrl(link.toString());
            urls.add(file);
        });
        exchange.getMessage().setBody(urls);
    }

}
