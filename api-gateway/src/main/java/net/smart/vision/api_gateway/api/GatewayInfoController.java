package net.smart.vision.api_gateway.api;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway")
public class GatewayInfoController {

    @GetMapping("/info")
    @PreAuthorize("hasAuthority('SCOPE_gateway.read')")
    public Mono<String> info() {
        return Mono.just("gateway-info");
    }

    @GetMapping("/health")
    public Mono<String> internalHealth() { return Mono.just("OK"); }

}

