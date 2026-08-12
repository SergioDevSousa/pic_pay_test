package br.com.picpay.dto.request;

import br.com.picpay.enums.UserType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UserRequest(
        @NotBlank String fullName,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{14}", message = "document deve conter 11 ou 14 dígitos") String document,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotNull @PositiveOrZero BigDecimal balance,
        @NotNull UserType userType
) {}
