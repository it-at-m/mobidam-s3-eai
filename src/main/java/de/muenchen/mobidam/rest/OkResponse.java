package de.muenchen.mobidam.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OkResponse {

    @JsonProperty("status")
    private Integer httpStatusCode;

    @JsonProperty("objects")
    private List<BucketContent> objects = new ArrayList<>();

}
