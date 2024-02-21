package de.muenchen.mobidam.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@ToString
public class ErrorResponse {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonProperty("timestamp")
    private Date timestamp;

    @JsonProperty("message")
    private String message;

    @JsonProperty("status")
    private Integer status;

}
