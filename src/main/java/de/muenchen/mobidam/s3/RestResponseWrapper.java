package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import de.muenchen.mobidam.rest.PresignedUrl;
import de.muenchen.mobidam.service.ArchiveService;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RestResponseWrapper implements Processor {

    private final ArchiveService archiveService;

    public RestResponseWrapper(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @Override
    public void process(Exchange exchange) throws MobidamException {

        var contextPath = exchange.getIn().getHeader(Constants.CAMEL_SERVLET_CONTEXT_PATH, String.class);
        switch (contextPath) {
        case Constants.CAMEL_SERVLET_CONTEXT_PATH_FILES_IN_FOLDER:
            var filesInFolder = archiveService.filesInFile(exchange);
            exchange.getMessage().setBody(filesInFolder);
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
