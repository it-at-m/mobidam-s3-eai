package de.muenchen.mobidam.s3;

import de.muenchen.mobidam.Constants;
import de.muenchen.mobidam.eai.common.exception.CommonError;
import de.muenchen.mobidam.eai.common.exception.ErrorResponseBuilder;
import de.muenchen.mobidam.eai.common.exception.MobidamException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.http.HttpStatus;

public abstract class OperationWrapper implements Processor {

    public abstract void process(Exchange exchange) throws Exception;

    public Exchange checkObjectName(Exchange exchange, String objectName) throws MobidamException {

        if (objectName == null) {
            CommonError error = ErrorResponseBuilder.build(HttpStatus.BAD_REQUEST.value(), "Object name is empty");
            exchange.getMessage().setBody(error);
            throw new MobidamException("Object name is empty");
        }
        return exchange;
    }
}
