package pl.lodz.p.it.ssbd2025.ssbd02;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/*
 TODO PURELY FOR TESTING REMOVE LATER
 TODO PURELY FOR TESTING REMOVE LATER
 TODO PURELY FOR TESTING REMOVE LATER
 TODO PURELY FOR TESTING REMOVE LATER
 TODO PURELY FOR TESTING REMOVE LATER
 TODO PURELY FOR TESTING REMOVE LATER
 */

@RestController
@RequestMapping("/api")
@EnableMethodSecurity(prePostEnabled = true)
public class ResourceServerController {
//    @GetMapping("/")
//    public String index(@AuthenticationPrincipal Jwt jwt) {
//        return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
//    }
//
//    @GetMapping("/client")
//    public String client(@AuthenticationPrincipal Jwt jwt) {
//        return String.format("Hello, %s!", jwt.getClaimAsString("preferred_username"));
//    }

    @GetMapping("/test/user")
    @PreAuthorize("hasRole('client_user')")
    public String test() {
        return "Hello keycloak user :)\n";
    }

    @GetMapping("/test/admin")
    @PreAuthorize("hasRole('client_admin')")
    public String test2() {
        return "Hello keycloak admin :)\n";
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('client_admin') || hasRole('client_user')")
    public String test3() {
        return "Hello keycloak user or admin :)\n";
    }

    @GetMapping("/test/normal/client")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    public String test4() {
        return "Hello user :)\n";
    }

    @GetMapping("/test/normal/admin")
    @PreAuthorize("hasRole('client_admin')")
    public String test5() {
        return "Hello admin :)\n";
    }

    @GetMapping("/test/normal")
    @PreAuthorize("hasRole('client_admin') || hasRole('client_user')")
    public String test6() {
        return "Hello user or admin :)\n";
    }

    @PostMapping("/removelater")
    public String register() {
        return "Hello anonymous :)\n";
    }
}
