package com.bcp.services.transaction.config.core.utils.utils;

import org.springframework.core.env.PropertyResolver;

import java.util.Optional;

/**
 * Dummy <br/>
 * <b>Class</b>: {@link PropertyUtils}<br/>
 *
 * @author vito.ivan <br/>
 * @version 1.0
 */
public final class PropertyUtils {
  private PropertyUtils() {
  }

  private static final String APPLICATION_CODE_PROPERTY = "spring.application.name";
  private static final String DEFAULT_APPLICATION_CODE = "unknown";

  private static PropertyResolver resolver;

  public static void setResolver(final PropertyResolver resolver0) {
    PropertyUtils.resolver = resolver0;
  }

  public static String getValue(final String property) {

    return resolver.getProperty(property);
  }

  public static <T> T getValue(final String property, final Class<T> resolvedClazz) {
    return resolver.getProperty(property, resolvedClazz);
  }

  public static <T> Optional<T> getOptionalValue(final String property, final Class<T> resolvedClazz) {
    return Optional.ofNullable(resolver.getProperty(property, resolvedClazz));
  }

  public static Optional<String> getOptionalValue(final String property) {
    return Optional.ofNullable(resolver.getProperty(property));
  }

  public static String getApplicationCode() {
    return getOptionalValue(APPLICATION_CODE_PROPERTY).orElse(DEFAULT_APPLICATION_CODE);
  }
}