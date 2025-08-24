package com.jc.api_gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ApiGatewayConfiguration {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
//                Route for user-service
                .route("user-service-route", r -> r.path("/users/**").uri("lb://user-service"))

//               Route for product-service
                .route("product-service-route", r -> r.path("/products/**").uri("lb://product-service"))

//               Route for user-service
                .route("order-service-route", r -> r.path("/orders/**").uri("lb://order-service"))

                .build();
    }
}

