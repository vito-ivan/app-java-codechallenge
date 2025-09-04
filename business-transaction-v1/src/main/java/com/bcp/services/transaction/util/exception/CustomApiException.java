package com.bcp.services.transaction.util.exception;

import com.bcp.services.transaction.config.core.utils.constants.ErrorCategory;
import com.bcp.services.transaction.config.core.utils.exception.ApiException;
import com.bcp.services.transaction.config.core.utils.utils.PropertyUtils;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

import static com.bcp.services.transaction.config.core.utils.constants.ErrorCategory.CONFLICT;
import static com.bcp.services.transaction.config.core.utils.constants.ErrorCategory.EXTERNAL_ERROR;
import static com.bcp.services.transaction.config.core.utils.constants.ErrorCategory.UNEXPECTED;
import static com.bcp.services.transaction.util.constants.Constants.BD_COMPONENT_NAME;
import static com.bcp.services.transaction.util.constants.Constants.DOT_AND_SPACE;
import static org.apache.logging.log4j.util.Strings.EMPTY;


/**
 * Builder component that contains methods to build exception object.
 *
 * @author vito.ivan
 */
@SuppressFBWarnings(value = "NM_CLASS_NOT_EXCEPTION", justification = "This class is derived from another exception")
@Getter
@AllArgsConstructor
public enum CustomApiException {

    C5001(EXTERNAL_ERROR,
            "C5001", BD_COMPONENT_NAME,
            "Error de base de datos. "),

    C4091(CONFLICT,
            "C4091", PropertyUtils.getApplicationCode(),
            "No se encontró ningúna transacción para el ID proporcionado."),

    C5003(UNEXPECTED,
            "C5003", PropertyUtils.getApplicationCode(),
            "Error al convertir un objeto a json. ");;

    private final ErrorCategory category;

    private final String code;
    private final String componentName;
    private final String description;

    /**
     * Return exception.
     *
     * @return ApiException
     */
    public ApiException getException() {

        return ApiException
                .builder()
                .category(this.getCategory())
                .systemCode(this.getCategory().getCode())
                .description(this.getCategory().getDescription())
                .errorType(this.getCategory().getErrorType())
                .addDetail(true)
                .withCode(this.getCode())
                .withComponent(this.getComponentName())
                .withDescription(this.getDescription())
                .push()
                .build();
    }

    /**
     * Build and return a custom exception with description.
     *
     * @param throwable throwable
     * @return ApiException
     */
    public ApiException getException(final Throwable throwable) {

        return ApiException
                .builder()
                .category(this.getCategory())
                .systemCode(this.getCategory().getCode())
                .description(this.getCategory().getDescription())
                .errorType(this.getCategory().getErrorType())
                .addDetail(true)
                .withCode(this.getCode())
                .withComponent(this.getComponentName())
                .withDescription(this.getDescription().concat(throwable.getClass().getName())
                        .concat(Optional.ofNullable(throwable.getMessage())
                                .map(DOT_AND_SPACE::concat).orElse(EMPTY))
                        .concat(Optional.ofNullable(throwable.getCause())
                                .map(th -> DOT_AND_SPACE.concat(th.getMessage())).orElse(EMPTY)))
                .push()
                .build();
    }

    /**
     * Build and return a custom exception with description.
     *
     * @param throwable throwable
     * @return ApiException
     */
    public ApiException getException(final String throwable) {

        return ApiException
                .builder()
                .category(this.getCategory())
                .systemCode(this.getCategory().getCode())
                .description(this.getCategory().getDescription())
                .errorType(this.getCategory().getErrorType())
                .addDetail(true)
                .withCode(this.getCode())
                .withComponent(this.getComponentName())
                .withDescription(this.getDescription().concat(throwable))
                .push()
                .build();
    }

}
