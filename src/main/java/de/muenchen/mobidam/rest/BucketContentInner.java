package de.muenchen.mobidam.rest;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BucketContentInner
 */

@JsonTypeName("BucketContent_inner")
@Generated(value = "org.openapitools.codegen.languages.JavaCamelServerCodegen", date = "2024-02-23T10:49:31.241207800+01:00[Europe/Berlin]")
public class BucketContentInner {

  private String key;

  private String lastmodified;

  private BigDecimal size;

  public BucketContentInner key(String key) {
    this.key = key;
    return this;
  }

  /**
   * Get key
   * @return key
  */
  
  @Schema(name = "key", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("key")
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public BucketContentInner lastmodified(String lastmodified) {
    this.lastmodified = lastmodified;
    return this;
  }

  /**
   * Get lastmodified
   * @return lastmodified
  */
  
  @Schema(name = "lastmodified", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("lastmodified")
  public String getLastmodified() {
    return lastmodified;
  }

  public void setLastmodified(String lastmodified) {
    this.lastmodified = lastmodified;
  }

  public BucketContentInner size(BigDecimal size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
  */
  @Valid 
  @Schema(name = "size", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("size")
  public BigDecimal getSize() {
    return size;
  }

  public void setSize(BigDecimal size) {
    this.size = size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BucketContentInner bucketContentInner = (BucketContentInner) o;
    return Objects.equals(this.key, bucketContentInner.key) &&
        Objects.equals(this.lastmodified, bucketContentInner.lastmodified) &&
        Objects.equals(this.size, bucketContentInner.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, lastmodified, size);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BucketContentInner {\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    lastmodified: ").append(toIndentedString(lastmodified)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

