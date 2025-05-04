package pl.lodz.p.it.ssbd2025.ssbd02.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.lodz.p.it.ssbd2025.ssbd02.mok.service.AccountService;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final AccountService accountService;
    private String issuer = "http://ssbd02-keycloak:8080/realms/app"; //make it prettier with @value or something

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        if (token != null && jwtTokenProvider.validateToken(token) && !Objects.equals(jwtTokenProvider.getIssuer(token), issuer)) { //last one makes sure it doesnt accidentally check oauth2 before its own dedicated filter
            if (jwtUtil.checkToken(token)) { //todo i think this needs to be fixed, to be fully thread-safe
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been invalidated");
                return;
            }

            String login = jwtTokenProvider.getLogin(token);
            List<String> roles = jwtTokenProvider.getRoles(token);

            UserDetails userDetails = accountService.loadUserByUsername(login);
            Collection<? extends GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
            System.out.println("Custom token accepted, context: " + SecurityContextHolder.getContext().getAuthentication());

            filterChain.doFilter(request, response);//THIS HAS TO STAY NO MATTER WHAT
            return;
        }
//        System.out.println("FILTER PASSED");
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
