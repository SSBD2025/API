package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.UnknownFilterException;
import pl.lodz.p.it.ssbd2025.ssbd02.exceptions.token.*;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.implementations.JwtService;

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
        if (token != null) {
            try {
                try {
                    jwtTokenProvider.validateToken(token);
                } catch (TokenBaseException e) {
                    handlerExceptionResolver.resolveException(request, response, null, e);
                    return;
                }
                if (Objects.equals(jwtTokenProvider.getIssuer(token), issuer)) {
                    // makes sure it doesnt accidentally check oauth2 before its own dedicated filter, skips altogether
                    filterChain.doFilter(request, response);
                    return;
                }
                if (jwtTokenProvider.getType(token).equals("access")) {
                    if (!jwtService.check(token)) {
                        handlerExceptionResolver.resolveException(request, response, null, new TokenNotFoundException());
                        return;
                    }

                    String login = jwtTokenProvider.getLogin(token);
                    List<String> roles = jwtTokenProvider.getRoles(token);

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
                if (jwtTokenProvider.getType(token).equals("access2fa")) {
                    if (!jwtService.check(token)) {
                        handlerExceptionResolver.resolveException(request, response, null, new TokenNotFoundException());
                        return;
                    }
                    String login = jwtTokenProvider.getLogin(token);
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
}
