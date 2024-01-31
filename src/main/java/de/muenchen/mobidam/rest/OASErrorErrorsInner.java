package de.muenchen.mobidam.rest;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * OASErrorErrorsInner
 */

@JsonTypeName("OASError_errors_inner")
@Generated(value = "org.openapitools.codegen.languages.JavaCamelServerCodegen", date = "2024-01-31T10:11:54.709538800+01:00[Europe/Berlin]")
public class OASErrorErrorsInner {

  private String path;

  private String message;

  private String errorCode;

  public OASErrorErrorsInner path(String path) {
    this.path = path;
    return this;
  }

  /**
   * For input validation errors, identifies where in the JSON request body the error occurred. 
   * @return path
  */
  
  @Schema(name = "path", description = "For input validation errors, identifies where in the JSON request body the error occurred. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("path")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public OASErrorErrorsInner message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Human-readable error message.
   * @return message
  */
  
  @Schema(name = "message", description = "Human-readable error message.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public OASErrorErrorsInner errorCode(String errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  /**
   * Code indicating error type.
   * @return errorCode
  */
  
  @Schema(name = "errorCode", description = "Code indicating error type.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("errorCode")
  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OASErrorErrorsInner oaSErrorErrorsInner = (OASErrorErrorsInner) o;
    return Objects.equals(this.path, oaSErrorErrorsInner.path) &&
        Objects.equals(this.message, oaSErrorErrorsInner.message) &&
        Objects.equals(this.errorCode, oaSErrorErrorsInner.errorCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, message, errorCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OASErrorErrorsInner {\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    errorCode: ").append(toIndentedString(errorCode)).append("\n");
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

