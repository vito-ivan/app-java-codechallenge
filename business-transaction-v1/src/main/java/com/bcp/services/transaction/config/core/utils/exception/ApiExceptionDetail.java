package com.bcp.services.transaction.config.core.utils.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Dummy <br/>
 * <b>Class</b>: {@link ApiExceptionDetail}<br/>
 *
 * @author vito.ivan <br/>
 * @version 1.0
 */
@ToString
@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Datos del error t&eacute;cnico.")
public class ApiExceptionDetail {

  @Schema(title = "Codigo de error del Detalle/Proveedor", example = "TL0008")
  private final String code;

  @Schema(title = "Nombre del componente de falla", example = "biz-sinister-query-compensationclaim-lpg-v1")
  private final String component;

  @Schema(title = "Descripcion del Detalle", example = "Codigo invalido para el canal")
  private final String description;

  @JsonIgnore
  private final boolean resolved;

  @Schema(title = "Endpoint que ejecuta el servicio", example = "(GET) /biz/sinister/query/compensation-claim/lpg/v1")
  private final String endpoint;

  /**
   * Create an ApiExceptionDetail instance.
   *
   * @param code0        Provider error code.
   * @param component0   Service provider code.
   * @param description0 Provider error description.
   * @param resolved0    Whether the detail will be resolved or not.
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  ApiExceptionDetail(
      @JsonProperty("code") final String code0,
      @JsonProperty("component") final String component0,
      @JsonProperty("description") final String description0,
      @JsonProperty("resolved") final boolean resolved0,
      @JsonProperty("endpoint") final String endpoint0) {
    this.code = code0;
    this.component = component0;
    this.description = description0;
    this.resolved = resolved0;
    this.endpoint = endpoint0;
  }
}
