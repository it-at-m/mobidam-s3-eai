package de.muenchen.mobidam.exception;

import de.muenchen.mobidam.rest.ErrorResponse;

import java.math.BigDecimal;
import java.util.Date;

public class ErrorResponseBuilder {

    public static ErrorResponse build(final Integer status, final String message) {
        var res = new ErrorResponse();
        res.setTimestamp(new Date());
        res.setStatus(BigDecimal.valueOf(status));
        res.setError(message);
        return res;
    }
}
