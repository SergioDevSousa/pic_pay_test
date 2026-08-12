package br.com.picpay;

import br.com.picpay.dto.response.AuthorizationResponse;
import br.com.picpay.repository.TransactionRepository;
import br.com.picpay.repository.UserRepository;
import br.com.picpay.service.AuthorizationService;
import br.com.picpay.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PicpayApplicationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @MockitoBean private AuthorizationService authorizationService;
    @MockitoBean private NotificationService notificationService;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();
        reset(authorizationService, notificationService);
    }

    @Test
    void shouldCreateUserWithoutExposingPassword() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(userJson("Ana", "12345678901", "ana@email.com", "COMMON", "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Ana"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void shouldTransferBetweenUsers() throws Exception {
        long payer = createUser("Pagador", "12345678901", "payer@email.com", "COMMON", "100.00");
        long payee = createUser("Recebedor", "12345678000199", "payee@email.com", "MERCHANT", "0.00");
        when(authorizationService.authorize()).thenReturn(true);

        mockMvc.perform(post("/transfer")
                        .contentType("application/json")
                        .content("{\"value\":25.00,\"payer\":" + payer + ",\"payee\":" + payee + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(25.00))
                .andExpect(jsonPath("$.payer").value(payer))
                .andExpect(jsonPath("$.payee").value(payee));

        mockMvc.perform(get("/users/{id}", payer)).andExpect(jsonPath("$.balance").value(75.00));
        mockMvc.perform(get("/users/{id}", payee)).andExpect(jsonPath("$.balance").value(25.00));
        verify(notificationService).notify(any());
    }

    @Test
    void shouldRejectMerchantAsPayer() throws Exception {
        long payer = createUser("Loja", "12345678000199", "loja@email.com", "MERCHANT", "100.00");
        long payee = createUser("Pessoa", "12345678901", "pessoa@email.com", "COMMON", "0.00");

        mockMvc.perform(post("/transfer")
                        .contentType("application/json")
                        .content("{\"value\":10.00,\"payer\":" + payer + ",\"payee\":" + payee + "}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Lojistas não podem realizar transferências"));
    }

    @Test
    void shouldReadAuthorizationJson() throws Exception {
        AuthorizationResponse response = objectMapper.readValue("""
                {"status":"success","data":{"authorization":true}}
                """, AuthorizationResponse.class);
        assertTrue(response.isAuthorized());
    }

    private long createUser(String name, String document, String email, String type, String balance) throws Exception {
        String response = mockMvc.perform(post("/users").contentType("application/json")
                        .content(userJson(name, document, email, type, balance)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String userJson(String name, String document, String email, String type, String balance) {
        return "{\"fullName\":\"" + name + "\",\"document\":\"" + document
                + "\",\"email\":\"" + email + "\",\"password\":\"123456\",\"balance\":"
                + balance + ",\"userType\":\"" + type + "\"}";
    }
}
