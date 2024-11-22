package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import de.muenchen.mobidam.rest.BucketContentInner;
import de.muenchen.mobidam.rest.PresignedUrl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

@Component
@Slf4j
public class RestResponseWrapper implements Processor {

    @Value("${mobidam.limit.search.items:20}")
    private int maxS3ObjectItems;

    @Override
    public void process(Exchange exchange) throws MobidamException {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class);
        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            filesInFile(exchange);
            break;
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_PRESIGNED_URL:
            presignedUrl(exchange);
            break;
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_ARCHIVE:
            exchange.getMessage().setBody(null);
            break;
        default:
            exchange.getMessage().setBody(ErrorResponseBuilder.build(HttpStatus.NOT_FOUND.value(), "REST ContextPath not found : " + contextPath));
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

    private void presignedUrl(Exchange exchange) throws MobidamException {

        var links = exchange.getIn().getBody(Collection.class);

        if (links.isEmpty()) {
            CommonError error = ErrorResponseBuilder.build(500, "Empty S3 url file list");
            exchange.getMessage().setBody(error);
            throw new MobidamException("Empty S3 url file list");
        } else {
            var file = new PresignedUrl();
            file.setUrl(links.iterator().next().toString());
            exchange.getMessage().setBody(file);
        }
    }

}
