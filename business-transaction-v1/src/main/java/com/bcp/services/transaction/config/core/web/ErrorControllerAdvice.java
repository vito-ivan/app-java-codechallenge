package com.bcp.services.transaction.config.core.web;

import com.bcp.services.transaction.config.core.utils.constants.ErrorCategory;
import com.bcp.services.transaction.config.core.utils.exception.ApiException;
import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.bcp.services.transaction.config.core.utils.constants.ErrorCategory.INVALID_REQUEST;

/**
 * ErrorControllerAdvice.
 * This class is used to handle exceptions.
 */
@RestControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(ApiException.class)
    protected Mono<ResponseEntity<ApiException>> handleCustomFrameworkException(
            final ApiException exception,
            final ServerWebExchange serverWebExchange) {
        if (StringUtils.isBlank(exception.getCode())
                && ((!exception.getExceptionDetails().isEmpty()
                && StringUtils.isBlank(exception.getExceptionDetails().get(0).getCode()))
                || exception.getExceptionDetails().isEmpty())) {
            var endpoint = serverWebExchange.getRequest().getMethod().name()
                    .concat(StringUtils.SPACE).concat(serverWebExchange.getRequest().getPath().value());

            var apiException = ApiException
                    .builder()
                    .category(exception.getCategory())
                    .systemCode(exception.getCategory().getCode())
                    .description(exception.getCategory().getDescription())
                    .errorType(exception.getCategory().getErrorType())
                    .cause(exception)
                    .addDetail(true)
                    .withEndpoint(endpoint)
                    .withComponent(PropertyUtils.getApplicationCode())
                    .push()
                    .build();
            return Mono.just(ResponseEntity.status(this.fromCategoryToHttpStatus(apiException.getCategory()))
                    .body(apiException));
        }
        return Mono.just(ResponseEntity
                .status(this.fromCategoryToHttpStatus(exception.getCategory()))
                .body(exception.mutate().build()));
    }

    private HttpStatus fromCategoryToHttpStatus(final ErrorCategory errorCategory) {
        if (errorCategory == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.valueOf(errorCategory.getHttpStatus());
        }
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected Mono<ResponseEntity<ApiException>> constraintViolationExceptionHandler(
            final ConstraintViolationException violationException,
            final ServerWebExchange serverWebExchange) {

        var endpoint = serverWebExchange.getRequest().getMethod().name()
                .concat(StringUtils.SPACE).concat(serverWebExchange.getRequest().getPath().value());

        var finalBuilder = ApiException.builder()
                .category(INVALID_REQUEST)
                .systemCode(INVALID_REQUEST.getCode())
                .description(INVALID_REQUEST.getDescription())
                .errorType(INVALID_REQUEST.getErrorType())
                .setComponentName(PropertyUtils.getApplicationCode())
                .setEndpoint(endpoint);
        violationException.getConstraintViolations()
                .forEach(constraintViolation -> finalBuilder.addDetail()
                        .withDescription(constraintViolation.getMessage()).push());

        var apiException = finalBuilder.build();
        return Mono.just(ResponseEntity
                .status(this.getHttpStatusFromCategory(apiException.getCategory()))
                .body(apiException));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    protected Mono<ResponseEntity<ApiException>> webExchangeBindExceptionHandler(
            final WebExchangeBindException webExchangeBindException,
            final ServerWebExchange serverWebExchange) {

        var endpoint = serverWebExchange.getRequest().getMethod().name()
                .concat(StringUtils.SPACE).concat(serverWebExchange.getRequest().getPath().value());

        var finalBuilder = ApiException.builder()
                .setComponentName(PropertyUtils.getApplicationCode())
                .errorType(INVALID_REQUEST.getErrorType())
                .setEndpoint(endpoint)
                .systemCode(INVALID_REQUEST.getCode())
                .description(INVALID_REQUEST.getDescription())
                .category(INVALID_REQUEST);
        webExchangeBindException.getAllErrors()
                .forEach(objectError -> finalBuilder.addDetail()
                        .withDescription(objectError.getDefaultMessage()).push());

        var apiException = finalBuilder.build();
        return Mono.just(ResponseEntity
                .status(this.getHttpStatusFromCategory(apiException.getCategory()))
                .body(apiException));
    }

    private HttpStatus getHttpStatusFromCategory(final ErrorCategory errorCategory) {
        if (errorCategory == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            return HttpStatus.valueOf(errorCategory.getHttpStatus());
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiException> handleCustomException(final Exception exception,
                                                              final ServerWebExchange serverWebExchange) {

        var endpoint = serverWebExchange.getRequest().getMethod().name()
                .concat(StringUtils.SPACE).concat(serverWebExchange.getRequest().getPath().value());

        var errorCategory = ErrorCategoryFromThrowable.mapExceptionToCategory(exception);

        var apiException = ApiException.builder()
                .setComponentName(PropertyUtils.getApplicationCode())
                .errorType(errorCategory.getErrorType())
                .setEndpoint(endpoint)
                .systemCode(errorCategory.getCode())
                .description(errorCategory.getDescription())
                .category(errorCategory)
                .cause(exception)
                .build();

        return ResponseEntity.status(this.fromCategoryToHttpStatus(apiException.getCategory()))
                .body(apiException);
    }

}
