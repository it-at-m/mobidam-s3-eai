package de.muenchen.mobidam.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * S3 bucket content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BucketContent {

    @JsonProperty("key")
    private String key;

    @JsonProperty("lastmodified")
    private String lastmodified;

    @JsonProperty("size")
    private Long size;

}
