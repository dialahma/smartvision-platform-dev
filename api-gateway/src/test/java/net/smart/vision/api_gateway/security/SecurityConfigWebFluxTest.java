package net.smart.vision.api_gateway.security;


import net.smart.vision.api_gateway.api.GatewayInfoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.anyString;

@WebFluxTest(controllers = GatewayInfoController.class, excludeAutoConfiguration = ReactiveOAuth2ResourceServerAutoConfiguration.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityConfigWebFluxTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void setUpJwtDecoder() {
        Mockito.when(reactiveJwtDecoder.decode(anyString()))
                .thenAnswer(invocation -> {
                    String token = invocation.getArgument(0, String.class);

                    var jwtBuilder = Jwt.withTokenValue(token)
                            .header("alg", "none");

                    if ("token-with-scope".equals(token)) {
                        jwtBuilder.claim("scope", "gateway.read");
                    } else {
                        jwtBuilder.claim("scope", "other.scope");
                    }

                    return Mono.just(jwtBuilder.build());
                });
    }

    @Test
    void contextLoads() {
        // Juste vérifier que le contexte démarre
    }

    /**
     * Sans Authorization -> 401 UNAUTHORIZED.
     */
    @Test
    void gatewayInfoShouldBeProtectedWhenNoToken() {
        webTestClient
                .get().uri("/api/gateway/info")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * Avec un token décodé sans le scope gateway.read -> 403 FORBIDDEN.
     */
    @Test
    void gatewayInfoShouldReturnForbiddenWithoutProperScope() {
        webTestClient
                .get().uri("/api/gateway/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-no-scope")
                .exchange()
                .expectStatus().isForbidden();
    }

    /**
     * Avec un token décodé contenant le scope gateway.read -> 200 OK.
     */
    @Test
    void gatewayInfoShouldBeAccessibleWithGatewayReadScope() {
        webTestClient
                .get().uri("/api/gateway/info")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-with-scope")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * /actuator/** doit rester public (pas 401/403).
     */
    @Test
    void actuatorEndpointsShouldBePublic() {
        webTestClient
                .get().uri("/actuator/health")
                .exchange()
                .expectStatus()
                .value(status -> {
                    assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                    assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                });
    }

    /**
     * Un endpoint quelconque protégé avec un token valide ne doit pas renvoyer 401/403.
     */
    @Test
    void otherEndpointsShouldBeAccessibleWithJwt() {
        webTestClient
                .get().uri("/api/secured")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-with-scope")
                .exchange()
                .expectStatus()
                .value(status -> {
                    assertNotEquals(HttpStatus.UNAUTHORIZED.value(), status);
                    assertNotEquals(HttpStatus.FORBIDDEN.value(), status);
                    assertEquals(HttpStatus.NOT_FOUND.value(), status);
                });
    }
}
