package pl.lodz.p.it.ssbd2025.ssbd02.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "account",
        indexes = {
        @Index(name = "login_index", columnList = "login"),
        @Index(name = "email_index", columnList = "email")
})
@SecondaryTable(name = "user_data")
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AbstractEntity {

    @Basic(optional = false)
    @NotBlank
    @Column(updatable = false, nullable = false, unique = true, length = AccountConsts.LOGIN_MAX)
    @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX)
    private String login;

    @Column(name = "password", nullable = false, length = 60)
    @Size(min = 8, max = 60)
    @ToString.Exclude
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 60)
    private String email;

    @Column(nullable = false)
    private boolean active = false;

    @Column(name = "last_successful_login", nullable = true)
    private Timestamp lastSuccessfulLogin;

    @Column(name = "last_failed_login", nullable = true)
    private Timestamp lastFailedLogin;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "language", nullable = true)
    private Language language;

    @Column(name = "last_successful_login_ip", length = 45, nullable = true)
    private String lastSuccessfulLoginIp;

    @Column(name = "last_failed_login_ip", length = 45, nullable = true)
    private String lastFailedLoginIp;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @ToString.Exclude
    private Collection<UserRole> userRoles = new ArrayList<>();

    @Column(name = "first_name", table = "user_data", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", table = "user_data", nullable = false, length = 50)
    private String lastName;

    @Column(name = "two_factor_auth", nullable = false)
    private boolean twoFactorAuth = false;

    @Column(name = "reminded", nullable = false)
    private boolean reminded = false;

    @Column(name = "login_attempts", nullable = false)
    private int loginAttempts = 0;

    @Column(name = "locked_until", nullable = true)
    private Timestamp lockedUntil;
}
