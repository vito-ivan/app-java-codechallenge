package com.bcp.services.transaction.config.core.web;

import com.bcp.services.transaction.config.core.utils.constants.ErrorCategory;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import static org.springframework.util.ClassUtils.isAssignable;

/**
 * ErrorCategoryFromThrowable.
 * This class is used to map exceptions to error categories.
 */
public final class ErrorCategoryFromThrowable {

  private ErrorCategoryFromThrowable() {
  }

  public static ErrorCategory mapExceptionToCategory(final Exception ex) {
    Class<? extends Exception> exClass = ex.getClass();
    if (isAssignable(exClass, UnknownHostException.class)
        || isAssignable(exClass, NoRouteToHostException.class)
        || isAssignable(exClass, MalformedURLException.class)
        || isAssignable(exClass, URISyntaxException.class)) {
      return ErrorCategory.HOST_NOT_FOUND;
    } else if (isAssignable(exClass, SocketTimeoutException.class)
        || isAssignable(exClass, SocketException.class)
        || isAssignable(exClass, TimeoutException.class)) {
      return ErrorCategory.EXTERNAL_TIMEOUT;
    } else if (isAssignable(exClass, ConnectException.class)) {
      return ErrorCategory.SERVICE_UNAVAILABLE;
    }
    return ErrorCategory.UNEXPECTED;
  }
}
