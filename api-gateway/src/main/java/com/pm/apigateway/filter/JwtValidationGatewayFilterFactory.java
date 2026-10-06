package com.pm.apigateway.filter;

import com.pm.apigateway.dto.AuthIdentityDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
  private final WebClient webClient;

  public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
      @Value("${auth.service.url}") String authServiceUrl) {
    this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
  }

  @Override
  public GatewayFilter apply(Object config) {
    return (exchange, chain) -> {
      String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authorization == null || !authorization.startsWith("Bearer ")) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
      }

      return webClient.get().uri("/introspect").header(HttpHeaders.AUTHORIZATION, authorization)
          .retrieve().bodyToMono(AuthIdentityDTO.class)
          .flatMap(identity -> {
            if (identity.email() == null || identity.role() == null || identity.userId() == null) {
              exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
              return exchange.getResponse().setComplete();
            }
            ServerHttpRequest request = exchange.getRequest().mutate().headers(headers -> {
              headers.remove("X-Authenticated-Email");
              headers.remove("X-Authenticated-Role");
              headers.remove("X-Authenticated-User-Id");
              headers.remove("X-Authenticated-Doctor-Id");
              headers.set("X-Authenticated-Email", identity.email());
              headers.set("X-Authenticated-Role", identity.role());
              headers.set("X-Authenticated-User-Id", identity.userId().toString());
              if (identity.doctorId() != null) headers.set("X-Authenticated-Doctor-Id", identity.doctorId().toString());
            }).build();
            return chain.filter(exchange.mutate().request(request).build());
          })
          .onErrorResume(WebClientResponseException.Unauthorized.class, error -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
          })
          .onErrorResume(WebClientResponseException.class, error -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
          })
          .onErrorResume(WebClientRequestException.class, error -> {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
          });
    };
  }
}
