package br.com.picpay.dto.response;

public record AuthorizationResponse(String status, AuthorizationData data) {
    public boolean isAuthorized() {
        return "success".equalsIgnoreCase(status) && data != null && data.authorization();
    }
}
