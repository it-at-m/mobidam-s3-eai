package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.MobidamException;
import de.muenchen.mobidam.rest.FilesInFolderGet200ResponseInner;
import de.muenchen.mobidam.rest.OASError;
import de.muenchen.mobidam.rest.OASErrorErrorsInner;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
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

        if (objects.size() > maxS3ObjectItems){

            var itemsExceedSwellInner = new OASErrorErrorsInner();
            itemsExceedSwellInner.setErrorCode(String.valueOf(HttpStatus.SC_CONFLICT));
            itemsExceedSwellInner.message(String.format("Do not supply more than %s objects per request", maxS3ObjectItems));
            itemsExceedSwellInner.path(String.format("%s?%s)", exchange.getIn().getHeader("CamelServletContextPath", String.class), exchange.getIn().getHeader("CamelHttpQuery", String.class)));

            var itemsExceedSwell = new OASError();
            itemsExceedSwell.setMessage(String.format("%s S3 Objects found in bucket. Number of available objects should not exceed %s items. Specify S3 search criteria, clean up bucket content or increase supply limit.", objects.size(), maxS3ObjectItems));
            itemsExceedSwell.addErrorsItem(itemsExceedSwellInner);
            exchange.getOut().setBody(itemsExceedSwell);
            return;
        }

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


