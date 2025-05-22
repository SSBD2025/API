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
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

import org.springframework.security.web.SecurityFilterChain;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.AuthEntryPoint;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.AuthEntryPointHandler;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtAuthFilter;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtOAuthConverter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtOAuthConverter jwtOAuthConverter;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthEntryPointHandler authEntryPointHandler;
    private final AuthEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, BearerTokenAuthenticationFilter.class)
//                .oauth2ResourceServer((oauth2) -> oauth2 //those 2 lines cause all the trouble
//                        .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtOAuthConverter)))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(HttpMethod.POST,
                                "/api/account",
                                "/api/account/refresh",
                                "/api/account/force/changePassword",
                                "/api/account/reset/password/request",
                                "/api/account/reset/password/**",
                                "/api/account/login",
                                "/api/account/me/enable2f",
                                "/api/account/me/disable2f",
                                "/api/account/auth/email/request",
                                "/api/account/auth/email",
                                "/api/client/register",
                                "/api/dietician/register"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/account/confirm-email",
                                "/api/account/revert-email-change",
                                "/api/account/verify",
                                "/api/account/unlock"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/account/login/2fa"
                        ).hasAuthority("2FA_AUTHORITY")
                        .requestMatchers(HttpMethod.POST,
                                "/api/account/changePassword",
                                "/api/account/logout",
                                "/api/account/change-email",
                                "/api/account/resend-change-email",
                                "/api/account/log-role-change",
                                "/api/account/{id}/block",
                                "/api/account/{id}/unblock"
                        ).hasAnyRole("ADMIN", "CLIENT", "DIETICIAN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/account/me",
                                "/api/account",
                                "/api/account/{id}"
                        ).hasAnyRole("ADMIN", "CLIENT", "DIETICIAN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/account/me",
                                "/api/account/{id}"
                        ).hasAnyRole("ADMIN", "CLIENT", "DIETICIAN")
                        .requestMatchers(HttpMethod.POST,
                                "/api/admin/register",
                                "/api/account/{id}/changePassword",
                                "/api/account/{id}/change-user-email"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/account/{accountId}/roles/admin",
                                "/api/account/{accountId}/roles/dietician",
                                "/api/account/{accountId}/roles/client"
                        ).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/account/{accountId}/roles/admin",
                                "/api/account/{accountId}/roles/dietician",
                                "/api/account/{accountId}/roles/client"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(authEntryPointHandler));
        return http.build();
    }
}
