package br.com.picpay.dto.response;

import br.com.picpay.enums.UserType;
import java.math.BigDecimal;

public record UserResponse(
        Long id,
        String fullName,
        String document,
        String email,
        BigDecimal balance,
        UserType userType
) {}
