package de.muenchen.mobidam.rest;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.annotation.Generated;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * bucketContent
 */

@Data
@ToString
@NoArgsConstructor
public class BucketContent {

    private String key;

    private String lastmodified;

    private BigDecimal size;

}
