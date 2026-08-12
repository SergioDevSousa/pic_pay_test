package br.com.picpay.service;

import br.com.picpay.dto.response.AuthorizationResponse;
import br.com.picpay.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class AuthorizationService {
    private final RestClient restClient;
    private final String authorizationUrl;

    public AuthorizationService(
            @Value("${picpay.authorization.url:https://util.devi.tools/api/v2/authorize}") String authorizationUrl) {
        this.restClient = RestClient.create();
        this.authorizationUrl = authorizationUrl;
    }

    public boolean authorize() {
        try {
            AuthorizationResponse response = restClient.get()
                    .uri(authorizationUrl)
                    .retrieve()
                    .body(AuthorizationResponse.class);
            return response != null && response.isAuthorized();
        } catch (RestClientException exception) {
            throw new BusinessException("Serviço autorizador indisponível", exception);
        }
    }
}
