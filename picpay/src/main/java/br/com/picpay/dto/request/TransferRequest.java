package br.com.picpay.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal value,
        @NotNull Long payer,
        @NotNull Long payee
) {}
