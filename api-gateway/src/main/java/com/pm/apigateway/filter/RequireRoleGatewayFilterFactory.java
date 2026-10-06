package com.pm.apigateway.filter;

import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RequireRoleGatewayFilterFactory extends AbstractGatewayFilterFactory<RequireRoleGatewayFilterFactory.Config> {
  public RequireRoleGatewayFilterFactory() { super(Config.class); }
  @Override public List<String> shortcutFieldOrder() { return List.of("role"); }
  @Override public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      String role = exchange.getRequest().getHeaders().getFirst("X-Authenticated-Role");
      if (config.getRole() == null || !config.getRole().equals(role)) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
      }
      return chain.filter(exchange);
    };
  }
  public static class Config {
    private String role;
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
  }
}
