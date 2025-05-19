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
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.UserRoleConsts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = AccountConsts.TABLE_NAME,
        indexes = {
                @Index(name = AccountConsts.LOGIN_INDEX, columnList = AccountConsts.COLUMN_LOGIN),
                @Index(name = AccountConsts.EMAIL_INDEX, columnList = AccountConsts.COLUMN_EMAIL)
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

    @Column(name = AccountConsts.COLUMN_PASSWORD, nullable = false, length = AccountConsts.PASSWORD_MAX)
    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX)
    @ToString.Exclude
    private String password;

    @Column(name = AccountConsts.COLUMN_EMAIL, nullable = false, unique = true, length = AccountConsts.EMAIL_MAX)
    private String email;

    @Column(nullable = false)
    private boolean active = AccountConsts.DEFAULT_ACTIVE;

    @Column(name = AccountConsts.COLUMN_LAST_SUCCESSFUL_LOGIN, nullable = true)
    private Timestamp lastSuccessfulLogin;

    @Column(name = AccountConsts.COLUMN_LAST_FAILED_LOGIN, nullable = true)
    private Timestamp lastFailedLogin;

    @Column(nullable = false)
    private boolean verified = AccountConsts.DEFAULT_VERIFIED;

    @Column(name = AccountConsts.COLUMN_LANGUAGE, nullable = true)
    private Language language;

    @Column(name = AccountConsts.COLUMN_LAST_SUCCESSFUL_LOGIN_IP, length = AccountConsts.IP_MAX, nullable = true)
    private String lastSuccessfulLoginIp;

    @Column(name = AccountConsts.COLUMN_LAST_FAILED_LOGIN_IP, length = AccountConsts.IP_MAX, nullable = true)
    private String lastFailedLoginIp;

    @OneToMany(mappedBy = UserRoleConsts.FIELD_ACCOUNT, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @ToString.Exclude
    private Collection<UserRole> userRoles = new ArrayList<>();

    @Column(name = "first_name", table = "user_data", nullable = false, length = AccountConsts.NAME_MAX)
    private String firstName;

    @Column(name = "last_name", table = "user_data", nullable = false, length = AccountConsts.NAME_MAX)
    private String lastName;

    @Column(name = AccountConsts.COLUMN_TWO_FACTOR_AUTH, nullable = false)
    private boolean twoFactorAuth = AccountConsts.DEFAULT_TWO_FACTOR_AUTH;

    @Column(name = AccountConsts.COLUMN_REMINDED, nullable = false)
    private boolean reminded = AccountConsts.DEFAULT_REMINDED;

    @Column(name = AccountConsts.COLUMN_LOGIN_ATTEMPTS, nullable = false)
    private int loginAttempts = AccountConsts.DEFAULT_LOGIN_ATTEMPTS;

    @Column(name = AccountConsts.COLUMN_LOCKED_UNTIL, nullable = true)
    private Timestamp lockedUntil;
}
