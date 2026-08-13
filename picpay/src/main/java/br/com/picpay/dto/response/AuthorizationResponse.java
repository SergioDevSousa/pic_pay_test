package br.com.picpay.dto.response;

public record AuthorizationResponse(
        String status,
        AuthorizationData data
) {
}
