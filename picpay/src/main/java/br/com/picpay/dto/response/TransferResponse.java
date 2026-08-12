package br.com.picpay.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        BigDecimal value,
        Long payer,
        Long payee,
        LocalDateTime createdAt
) {}
