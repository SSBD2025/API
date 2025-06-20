package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.SensitiveDTO;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.UnknownFilterException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.JwtService;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.ExceptionConsts;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtService jwtService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (!requiresAuthentication(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (token == null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        try {
            try {
                jwtTokenProvider.validateToken(new SensitiveDTO(token));
            } catch (TokenBaseException e) {
                handlerExceptionResolver.resolveException(request, response, null, e);
                return;
            }
            if (Objects.equals(jwtTokenProvider.getIssuer(new SensitiveDTO(token)), issuer)) {
                filterChain.doFilter(request, response);
                return;
            }
            if (jwtTokenProvider.getType(new SensitiveDTO(token)).equals("access")) {
                if (!jwtService.check(token)) {
                    handlerExceptionResolver.resolveException(request, response, null, new TokenNotFoundException());
                    return;
                }

                String login = jwtTokenProvider.getLogin(new SensitiveDTO(token));
                List<String> roles = jwtTokenProvider.getRoles(new SensitiveDTO(token));

                Collection<? extends GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UserDetails userDetails = new UserDetailsImpl(authorities, null, login);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);

                filterChain.doFilter(request, response);
                return;
            }
            if (jwtTokenProvider.getType(new SensitiveDTO(token)).equals("access2fa")) {
                if (!jwtService.check(token)) {
                    handlerExceptionResolver.resolveException(request, response, null, new TokenNotFoundException());
                    return;
                }
                String login = jwtTokenProvider.getLogin(new SensitiveDTO(token));
                Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("2FA_AUTHORITY"));

                Authentication auth = new UsernamePasswordAuthenticationToken(login, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                filterChain.doFilter(request, response);
                return;
            }
        } catch (JwtException | IllegalArgumentException e) {
            handlerExceptionResolver.resolveException(request, response, null, new UnknownFilterException(e));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("POST".equals(method)) {
            List<String> publicPosts = List.of(
                    "/api/account",
                    "/api/account/refresh",
                    "/api/account/force/changePassword",
                    "/api/account/reset/password/request",
                    "/api/account/login",
                    "/api/client/register",
                    "/api/dietician/register"
            );
            if (publicPosts.contains(path) || path.startsWith("/api/account/reset/password/")) {
                return false;
            }
        }
        if ("GET".equals(method)) {
            List<String> publicGets = List.of(
                    "/api/account/confirm-email",
                    "/api/account/revert-email-change",
                    "/api/account/verify",
                    "/api/account/unlock"
            );
            if (publicGets.contains(path)) {
                return false;
            }
        }
        return !path.startsWith("/swagger-ui") &&
                !path.equals("/error") &&
                !path.equals("/favicon.ico") &&
                !path.startsWith("/v3/api-docs");
    }
}
