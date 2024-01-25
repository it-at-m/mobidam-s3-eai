package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.MobidamException;
import de.muenchen.mobidam.rest.FilesInFolderGet200ResponseInner;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class RestResponseWrapper implements Processor {
    @Override

    public void process(Exchange exchange) throws Exception {


        try {
            var contextPath = exchange.getIn().getHeader("CamelServletContextPath", String.class).replace("/", "");
            switch (contextPath) {
                case "filesInFolder":
                    filesInFile(exchange);
                    break;
                default:
                    exchange.setException(new MobidamException("REST ContextPath not found : " + contextPath));
            }
        } catch (Exception ex) {
            exchange.setException(new MobidamException("RestResponseWrapper failed.", ex));
        }

    }

    private void filesInFile(Exchange exchange) {

        var objects = exchange.getIn().getBody(Collection.class);

        var files = new ArrayList<FilesInFolderGet200ResponseInner>();

        objects.forEach(s3object -> {
            var file = new FilesInFolderGet200ResponseInner();
            file.setKey(((S3Object)s3object).key());
            file.setLastmodified(((S3Object)s3object).lastModified().toString());
            file.setSize(new BigDecimal(((S3Object)s3object).size()));
            files.add(file);

        });
        exchange.getOut().setBody(files);
    }


}


