package pl.lodz.p.it.ssbd2025.ssbd02.helpers;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@TestComponent
public class ModTestHelper {
    private SecurityContext securityContext;

    public void setClientContext() {
        securityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("agorgonzola", null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))));
    }

    public void setAdminContext() {
        securityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("jcheddar", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    public void setDieticianContext() {
        securityContext = SecurityContextHolder.getContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("tcheese", null, List.of(new SimpleGrantedAuthority("ROLE_DIETICIAN"))));
    }

    public void resetContext() {
        SecurityContextHolder.setContext(securityContext);
    }
}
