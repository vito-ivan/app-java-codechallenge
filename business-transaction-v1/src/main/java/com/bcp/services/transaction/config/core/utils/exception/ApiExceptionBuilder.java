package com.bcp.services.transaction.config.core.utils.exception;

import com.bcp.services.transaction.config.core.utils.constants.ErrorCategory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.ClassUtils.isAssignable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Dummy <br/>
 * <b>Class</b>: {@link ApiExceptionBuilder}<br/>
 *
 * @author vito.ivan <br/>
 * @version 1.0
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class ApiExceptionBuilder {

  private String componentName;
  private String endpoint;

  private ErrorCategory category;
  private String systemCode;
  private String description;
  private String errorType;
  private Throwable cause;

  private final List<ApiExceptionDetail> apiExceptionDetails = new ArrayList<>();
  private final Map<String, Object> properties = new ConcurrentHashMap<>();
  private final HttpHeaders headers = new HttpHeaders();

  private boolean hasNewCategory = false;
  private boolean hasNewErrorType = false;
  private boolean hasNewSystemCode = false;
  private boolean hasNewDescription = false;
  private boolean hasNewProperty = false;
  private boolean hasNewHeader = false;

  private boolean isMutated = false;
  private boolean isResolved = false;

  public ApiExceptionBuilder setComponentName(final String componentName0) {
    this.componentName = componentName0;
    return this;
  }

  public ApiExceptionBuilder setEndpoint(final String endpoint0) {
    this.endpoint = endpoint0;
    return this;
  }

  /**
   * Sets whether this builder is coming from an existing {@link ApiException}.
   */
  void setMutated(final boolean mutated0) {
    isMutated = mutated0;
  }

  /**
   * Checks whether this builder is coming from an existing
   * {@link ApiException}.
   *
   * @return True if it is mutated.
   */
  public boolean isMutated() {
    return isMutated;
  }

  /**
   * Set error category for new ApiException.
   *
   * @param category0 Error category to configure.
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder category(final ErrorCategory category0) {
    if (category0 != null) {
      this.hasNewCategory = true;
      this.category = category0;
    }
    return this;
  }

  /**
   * Set error type for new ApiException.
   *
   * @param errorType0 Error type to configure.
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder errorType(final String errorType0) {
    if (isNotBlank(errorType0)) {
      this.hasNewErrorType = true;
      this.errorType = errorType0;
    }
    return this;
  }

  /**
   * Set description for new ApiException.
   *
   * @param description0 Description to configure.
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder description(final String description0) {
    if (isNotBlank(description0)) {
      this.hasNewDescription = true;
      this.description = description0;
    }
    return this;
  }

  /**
   * Set System code for new ApiException.
   *
   * @param systemCode0 System code to configure.
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder systemCode(final String systemCode0) {
    if (isNotBlank(systemCode0)) {
      this.hasNewSystemCode = true;
      this.systemCode = systemCode0;
    }
    return this;
  }

  /**
   * Insert a pair key-value as a HTTP Header in the exception.
   *
   * @param key0   The entry key associated.
   * @param value0 The value associated to key.
   */
  public void addHeader(final String key0, final String value0) {
    if (isNotBlank(key0) && StringUtils.isAsciiPrintable(key0)) {
      this.hasNewHeader = true;
      headers.add(key0, value0);
    }
  }

  /**
   * Insert a pair key-value as a custom field in the exception.
   *
   * @param key   The entry key associated.
   * @param value The value associated to key.
   */
  public void addProperty(final String key, final Object value) {
    if (isNotBlank(key) && StringUtils.isAsciiPrintable(key) && isValidForProperty(value)) {
      this.hasNewProperty = true;
      properties.put(key, value);
    }
  }

  private static boolean isValidForProperty(final Object value) {
    return value != null && (ClassUtils.isPrimitiveOrWrapper(value.getClass())
        || isAssignable(value.getClass(), CharSequence.class)
        || isAssignable(value.getClass(), Date.class)
        || isAssignable(value.getClass(), Calendar.class)
        || isAssignable(value.getClass(), Instant.class)
        || isAssignable(value.getClass(), Timestamp.class));
  }

  /**
   * Set base exception for new ApiException.
   *
   * @param cause0 Base Exception to configure.
   * @return An ApiExceptionBuilder instance.
   */
  public ApiExceptionBuilder cause(final Throwable cause0) {
    this.cause = cause0;
    return this;
  }

  private void addExceptionDetail(final ApiExceptionDetail detail) {
    this.apiExceptionDetails.add(detail);
    this.isResolved = false;
  }

  /**
   * Mark new ApiException as resolved.
   */
  public void markAsResolved() {
    this.isResolved = true;
  }

  /**
   * Returns the builder for create a new {@link ApiExceptionDetail}.
   *
   * @return The {@link ApiExceptionDetailBuilder} instance.
   */
  public ApiExceptionDetailBuilder addDetail() {
    return this.addDetail(false);
  }

  /**
   * Returns the builder for create a new {@link ApiExceptionDetail}.
   *
   * @param resolved Whether the detail is marked as resolved or not.
   * @return The {@link ApiExceptionDetailBuilder} instance.
   */
  public ApiExceptionDetailBuilder addDetail(final boolean resolved) {
    return new ApiExceptionDetailBuilder(this, resolved);
  }

  /**
   * dummy.
   *
   * @author willianmarchan
   */
  public static final class ApiExceptionDetailBuilder {

    private String code;
    private String component;
    private String description;
    private Boolean resolved;
    private String endpoint;

    private final ApiExceptionBuilder builder;

    private ApiExceptionDetailBuilder(final ApiExceptionBuilder builder0, final boolean resolved0) {
      this.builder = builder0;
      this.resolved = resolved0;
    }

    /**
     * Set the {@code code} for {@link ApiExceptionDetail}.
     *
     * @param code0 The component to be set.
     * @return The {@link ApiExceptionDetailBuilder} instance.
     */
    public ApiExceptionDetailBuilder withCode(final String code0) {
      if (isNotBlank(code0)) {
        this.code = code0;
      }
      return this;
    }

    /**
     * Set the {@code component} for {@link ApiExceptionDetail}.
     *
     * @param component0 The component to be set.
     * @return The {@link ApiExceptionDetailBuilder} instance.
     */
    public ApiExceptionDetailBuilder withComponent(final String component0) {
      if (isNotBlank(component0)) {
        this.component = component0;
      }
      return this;
    }

    /**
     * Set the {@code endpoint} for {@link ApiExceptionDetail}.
     *
     * @param endpoint0 The component to be set.
     * @return The {@link ApiExceptionDetailBuilder} instance.
     */
    public ApiExceptionDetailBuilder withEndpoint(final String endpoint0) {
      if (isNotBlank(endpoint0)) {
        this.endpoint = endpoint0;
      }
      return this;
    }

    /**
     * Set the {@code description} for {@link ApiExceptionDetail}.
     *
     * @param description0 The description to be set.
     * @return The {@link ApiExceptionDetailBuilder} instance.
     */
    public ApiExceptionDetailBuilder withDescription(final String description0) {
      if (isNotBlank(description0)) {
        this.description = description0;
      }
      return this;
    }

    /**
     * Creates and Adds a new {@link ApiExceptionDetail} with the parameters
     * previosly setted.
     *
     * @return The {@link ApiExceptionBuilder} instance.
     */
    public ApiExceptionBuilder push() {
      component = component == null ? this.builder.componentName : component;
      endpoint = endpoint == null ? this.builder.endpoint : endpoint;
      if (isNotBlank(code) || isNotBlank(component) || isNotBlank(description)
          || isNotBlank(endpoint)) {
        builder.addExceptionDetail(
            new ApiExceptionDetail(code, component, description, resolved, endpoint));
        this.code = null;
        this.component = null;
        this.description = null;
        this.resolved = null;
      }
      return this.builder;
    }
  }

  /**
   * Create a new instance of ApiException.
   *
   * @return An ApiException instance.
   */
  public ApiException build() {

    Boolean isResolvedFlag = isResolved;

    if (cause != null) {
      if (cause instanceof ApiException apiException) {
        apiException.getUnresolvedExceptionDetails()
            .forEach(detail -> addDetail(detail.isResolved())
                .withCode(detail.getCode())
                .withComponent(detail.getComponent())
                .withDescription(detail.getDescription())
                .withEndpoint(detail.getEndpoint())
                .push());
        cause(null);
      } else if (!isMutated()) {
        addDetail(true)
            .withComponent(componentName)
            .withEndpoint(endpoint)
            .withDescription(cause.getClass().getName()
                .concat(Optional.ofNullable(cause.getMessage()).map(" : "::concat).orElse("")))
            .push();
        cause(null);
      }
    }

    return this.createApiExceptionInstance(isResolvedFlag);
  }

  private ApiException createApiExceptionInstance(final Boolean isResolvedFlag) {
    return new ApiException(hasNewSystemCode ? systemCode : null,
        // Avoid to call error catalog / with a previous code
        hasNewDescription ? description : null,
        hasNewErrorType ? errorType : null,
        hasNewCategory ? category : ErrorCategory.UNEXPECTED,
        apiExceptionDetails,
        hasNewProperty ? properties : null,
        hasNewHeader ? headers : null,
        cause,
        isResolvedFlag);
  }

  public Mono buildAsCompletable() {
    return Mono.error(this::build);
  }

  public <T> Mono<T> buildAsSingle() {
    return Mono.error(this::build);
  }

  public <T> Flux<T> buildAsObservable() {
    return Flux.error(this::build);
  }

  public <T> Flux<T> buildAsMaybe() {
    return Flux.error(this::build);
  }

  public <T> Flux<T> buildAsFlowable() {
    return Flux.error(this::build);
  }
}
