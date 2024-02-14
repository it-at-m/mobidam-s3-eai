package de.muenchen.mobidam.rest;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * bucketContent
 */

@Data
@NoArgsConstructor
@ToString
public class BucketContent {

    private String key;

    private String lastmodified;

    private BigDecimal size;

}
