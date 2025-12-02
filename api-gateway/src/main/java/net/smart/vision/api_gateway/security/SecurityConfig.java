package net.smart.vision.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // pour @PreAuthorize sur GatewayInfoController
public class SecurityConfig {

    /**
     * Chaîne de sécurité dédiée aux endpoints Actuator : tout est permis.
     */
    @Bean
    @Order(0)
    public SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    /**
     * Chaîne de sécurité principale pour l’API Gateway.
     */
    @Bean
    @Order(1)
    public SecurityWebFilterChain apiSecurityFilterChain(
            ServerHttpSecurity http,
            Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter
    ) {

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        // L’endpoint info est protégé par @PreAuthorize dans le controller,
                        // ici on demande juste une authentification
                        .pathMatchers("/api/gateway/info").authenticated()
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
                )
                .build();
    }

    /**
     * Converter JWT (Keycloak-style) → authorities réactives.
     * On transforme le JwtAuthenticationConverter (servlet) en converter réactif
     * via ReactiveJwtAuthenticationConverterAdapter.
     */
    @Bean
    public Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthConverter() {
        JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();
        delegate.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Object scopeClaim = jwt.getClaims().get("scope");
            if (scopeClaim instanceof String scopeStr) {
                Arrays.stream(scopeStr.split(" "))
                        .filter(s -> !s.isBlank())
                        .forEach(s ->
                                authorities.add(new SimpleGrantedAuthority("SCOPE_" + s))
                        );
            }

            return authorities;
        });

        // 🔴 ICI la magie : adaptation en converter réactif
        return new ReactiveJwtAuthenticationConverterAdapter(delegate);
    }
}
