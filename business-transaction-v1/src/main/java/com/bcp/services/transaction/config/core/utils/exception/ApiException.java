package com.bcp.services.transaction.config.core.utils.exception;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.bcp.services.transaction.config.core.utils.constants.ErrorCategory;
import com.bcp.services.transaction.config.core.utils.constants.HttpHeadersKey;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dummy <br/>
 * <b>Class</b>: {@link ApiException}<br/>
 *
 * @author vito.ivan <br/>
 * @version 1.0
 */
@SuppressFBWarnings(value = "SE_TRANSIENT_FIELD_NOT_RESTORED",
    justification = "Transient field that isn't set by deserialization.")
@Getter
@JsonAutoDetect(creatorVisibility = Visibility.NONE,
    fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE,
    isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonInclude(Include.NON_NULL)
@Schema(description = "Datos del error de sistema.")
@Slf4j
public class ApiException extends RuntimeException {

  @JsonProperty
  @Schema(title = "Codigo de error de Sistema", example = "TL0001")
  private final String code;

  @JsonProperty
  @Schema(title = "Descripcion del error de Sistema", example = "Error al llamar al servicio")
  private final String description;

  @JsonProperty
  @Schema(title = "Tipo de Error de Sistema", example = "TECHNICAL")
  private final String errorType;

  @JsonProperty
  @Schema(title = "Lista de detalles del error")
  private final List<ApiExceptionDetail> exceptionDetails;

  @JsonProperty
  @JsonIgnore
  private final Map<String, Object> properties;

  @JsonIgnore
  private final HttpHeaders headers;

  @Schema(title = "Categoria del error", example = "INVALID_REQUEST")
  @JsonProperty
  private final ErrorCategory category;

  @JsonIgnore
  private final boolean isResolved;

  @SuppressFBWarnings(
      justification = "generated code"
  )
  @JsonCreator
  private ApiException(
      final @JsonProperty(value = "code", required = true) String code01,
      final @JsonProperty(value = "description", required = true) String description01,
      final @JsonProperty("errorType") String errorType01,
      final @JsonProperty("exceptionDetails") List<ApiExceptionDetail> exceptionDetails01,
      final @JsonProperty("properties") Map<String, Object> properties01) {

    this.code = code01;
    this.description = description01;
    this.errorType = errorType01;
    this.headers = null;
    this.category = null;
    this.exceptionDetails = Optional.ofNullable(exceptionDetails01).map(Collections::unmodifiableList).orElseGet(
        Collections::emptyList);
    this.properties = properties01;
    this.isResolved = true;
  }

  @SuppressFBWarnings(
      justification = "generated code"
  )
  ApiException(
      final String code01,
      final String description01,
      final String errorType01,
      final ErrorCategory category01,
      final List<ApiExceptionDetail> exceptionDetails01,
      final Map<String, Object> properties01,
      final HttpHeaders headers01,
      final Throwable cause01,
      final boolean resolved01) {

    super(description01, cause01);

    this.code = code01;
    this.description = description01;
    this.errorType = errorType01;
    this.category = category01;
    this.exceptionDetails = Optional.ofNullable(exceptionDetails01).map(Collections::unmodifiableList)
        .orElseGet(Collections::emptyList);
    this.properties = properties01;
    this.headers = headers01;
    this.isResolved = resolved01;
    if (headers != null) {
      this.putMdc(headers);
    }
    MDC.put("code", code);
    MDC.put("description", description);
    MDC.put("errorType", errorType);
    exceptionDetails.forEach(this::setMdcProperties);
    if (properties != null) {
      properties.forEach((key, value) -> MDC.put(key, value.toString()));
    }
  }

  private void putMdc(final HttpHeaders headers0) {
    List<String> validHeaders = new ArrayList<>();
    validHeaders.add(HttpHeadersKey.REQUEST_ID);
    validHeaders.add(HttpHeadersKey.APP_CODE);
    validHeaders.add(HttpHeadersKey.REQUEST_DATE);
    validHeaders.add(HttpHeadersKey.CALLER_NAME);

    if (headers0 != null) {
      headers0.forEach((key, value) -> {
        MDC.clear();
        if (validHeaders.contains(key)) {
          MDC.put(key, value.get(value.size() - 1));
        } else {
          log.debug("HttpHeadersKey not match");
        }
      });
    }

  }

  private void setMdcProperties(final ApiExceptionDetail value) {
    if (value.getCode() != null) {
      MDC.put("exceptionDetails.code", value.getCode());
    } else if (value.getComponent() != null) {
      MDC.put("exceptionDetails.component", value.getComponent());
    } else if (value.getDescription() != null) {
      MDC.put("exceptionDetails.description", value.getDescription());
    } else if (value.getEndpoint() != null) {
      MDC.put("exceptionDetails.endpoint", value.getEndpoint());
    }
  }

  /**
   * If the parent {@code cause} is an {@link ApiException}, joins with the
   * current list.
   *
   * @return A list of {@link ApiExceptionDetail}
   */
  @JsonProperty("exceptionDetails")
  public List<ApiExceptionDetail> getExceptionDetails() {
    if (getCause() instanceof ApiException apiException) {
      List<ApiExceptionDetail> details = apiException.getExceptionDetails();
      List<ApiExceptionDetail> newDetails = new ArrayList<>();
      newDetails.addAll(exceptionDetails);
      newDetails.addAll(details);
      return Collections.unmodifiableList(newDetails);
    }
    return new ArrayList<>(exceptionDetails);
  }

  /**
   * Get the original list of {@link ApiExceptionDetail} not including the
   * {@link Throwable} cause.
   *
   * @return A list of {@link ApiExceptionDetail}
   */
  public List<ApiExceptionDetail> getUnresolvedExceptionDetails() {
    return new ArrayList<>(exceptionDetails);
  }

  /**
   * Create an ApiExceptionBuilder instance.
   *
   * @return An ApiExceptionBuilder instance.
   */
  public static ApiExceptionBuilder builder() {
    return new ApiExceptionBuilder();
  }

  /**
   * Create an ApiExceptionBuilder instance.
   *
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder mutate() {

    ApiExceptionBuilder builder = builder()
        .cause(isResolved() ? this : getCause())
        .category(getCategory())
        .errorType(getErrorType())
        .systemCode(getCode())
        .description(getDescription());

    if (properties != null) {
      properties.forEach(builder::addProperty);
    }

    if (headers != null) {
      headers.forEach((key, value1) -> {
        for (String value : value1) {
          builder.addHeader(key, value);
        }
      });
    }

    builder.setMutated(true);

    if (isResolved()) {
      builder.markAsResolved();
    } else {
      if (null != exceptionDetails) {
        exceptionDetails.forEach(detail -> builder.addDetail(detail.isResolved())
            .withComponent(detail.getComponent())
            .withEndpoint(detail.getEndpoint())
            .withCode(detail.getCode())
            .withDescription(detail.getDescription())
            .push());
      }
    }
    return builder;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiException {\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    errorType: ").append(toIndentedString(errorType)).append("\n");
    sb.append("    exceptionDetails: ").append(toIndentedString(exceptionDetails)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(final Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
