package net.smart.vision.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

    @MockitoBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Test
    void contextLoads() {
        // Juste vérif de démarrage du contexte
    }
}

