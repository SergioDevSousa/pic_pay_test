package br.com.picpay.service;

import br.com.picpay.entity.Transaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import java.util.Map;

@Service
public class NotificationService {
    private final RestClient restClient;
    private final String notificationUrl;

    public NotificationService(
            @Value("${picpay.notification.url:https://util.devi.tools/api/v1/notify}") String notificationUrl) {
        this.restClient = RestClient.create();
        this.notificationUrl = notificationUrl;
    }

    public void notify(Transaction transaction) {
        try {
            restClient.post().uri(notificationUrl)
                    .body(Map.of("transactionId", transaction.getId(), "payee", transaction.getPayee().getId()))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException ignored) {
            // A transferência já foi concluída; indisponibilidade de notificação não a desfaz.
        }
    }
}
