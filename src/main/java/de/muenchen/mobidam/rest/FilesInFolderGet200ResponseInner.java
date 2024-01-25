package de.muenchen.mobidam.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * FilesInFolderGet200ResponseInner
 */

@JsonTypeName("_filesInFolder_get_200_response_inner")
@Generated(value = "org.openapitools.codegen.languages.JavaCamelServerCodegen", date = "2024-01-24T14:26:59.676107700+01:00[Europe/Berlin]")
public class FilesInFolderGet200ResponseInner {

  private String key;

  private String lastmodified;

  private BigDecimal size;

  public FilesInFolderGet200ResponseInner key(String key) {
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

  public FilesInFolderGet200ResponseInner lastmodified(String lastmodified) {
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

  public FilesInFolderGet200ResponseInner size(BigDecimal size) {
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
    FilesInFolderGet200ResponseInner filesInFolderGet200ResponseInner = (FilesInFolderGet200ResponseInner) o;
    return Objects.equals(this.key, filesInFolderGet200ResponseInner.key) &&
        Objects.equals(this.lastmodified, filesInFolderGet200ResponseInner.lastmodified) &&
        Objects.equals(this.size, filesInFolderGet200ResponseInner.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, lastmodified, size);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FilesInFolderGet200ResponseInner {\n");
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

