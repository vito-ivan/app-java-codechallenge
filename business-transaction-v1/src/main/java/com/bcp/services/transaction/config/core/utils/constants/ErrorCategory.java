package com.bcp.services.transaction.config.core.utils.constants;

import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import lombok.Getter;

import static com.bcp.services.transaction.config.core.utils.constants.Constants.TECHNICAL_ERROR;

/**
 * Dummy <br/>
 * <b>Class</b>: {@link ErrorCategory}<br/>
 *
 * @author vito.ivan <br/>
 * @version 1.0
 */

public enum ErrorCategory {

  INVALID_REQUEST("invalid-request", 400),
  ARGUMENT_MISMATCH("argument-mismatch", 400),
  UNAUTHORIZED("unauthorized", 401),
  FORBIDDEN("forbidden", 403),
  RESOURCE_NOT_FOUND("resource-not-found", 404),
  CONFLICT("conflict", 409),
  PRECONDITION_FAILED("precondition-failed", 412),
  EXTERNAL_ERROR("external-error", 500),
  HOST_NOT_FOUND("host-not-found", 500),
  UNEXPECTED("unexpected", 500),
  NOT_IMPLEMENTED("not-implemented", 501),
  SERVICE_UNAVAILABLE("service-unavailable", 503),
  EXTERNAL_TIMEOUT("external-timeout", 503);

  private static final String PROPERTY_PREFIX = "application.api.error-code.";
  private final String property;
  @Getter
  private final int httpStatus;

  ErrorCategory(final String property000, final int httpStatus000) {
    this.property = PROPERTY_PREFIX.concat(property000);
    this.httpStatus = httpStatus000;
  }

  private String codeProperty() {
    return this.property + ".code";
  }

  private String descriptionProperty() {
    return this.property + ".description";
  }

  private String errorTypeProperty() {
    return this.property + ".error-type";
  }

    public String getErrorType() {
    return PropertyUtils.getOptionalValue(this.errorTypeProperty()).orElse(TECHNICAL_ERROR);
  }

  public String getCode() {
    return PropertyUtils.getOptionalValue(this.codeProperty()).orElse("TL9999");
  }

  public String getDescription() {
    return PropertyUtils.getOptionalValue(this.descriptionProperty()).orElse("Sin descripcion configurada.");
  }
}