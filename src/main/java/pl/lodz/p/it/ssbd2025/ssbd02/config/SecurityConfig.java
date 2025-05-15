package pl.lodz.p.it.ssbd2025.ssbd02.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import org.springframework.security.web.SecurityFilterChain;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtAuthFilter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtOAuthConverter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtOAuthConverter jwtOAuthConverter;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, BearerTokenAuthenticationFilter.class)
                //to run locally: comment following 2 lines (.oauth2 ... .jwt...), comment line 5 uncomment line 6 in application.properties
//                .oauth2ResourceServer((oauth2) -> oauth2 //those 2 lines cause all the trouble
//                        .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtOAuthConverter)))
                .authorizeHttpRequests((authorize) ->
                        authorize
                                .requestMatchers(HttpMethod.POST,
                                "/api/client/register",
                                "/api/dietician/register",
                                "/api/account/login",
                                "/api/account/refresh",
                                "/api/account/reset/password/**",
                                "/api/account/reset/password/request",
                                "/api/account/login/2fa"
                                ).permitAll()
                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**",
                                        "/error",
                                        "/api/account/confirm-email/**",
                                        "/api/account/revert-email-change/**",
                                        "/api/account/verify/**",
                                        "/api/account/verify/",
                                        "/favicon.ico"
                                ).permitAll()
//                                .requestMatchers(HttpMethod.GET,
//                                        "/api/account/me"
//                                ).hasAnyRole("ADMIN", "CLIENT", "DIETICIAN")
                                .anyRequest().authenticated())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
}
