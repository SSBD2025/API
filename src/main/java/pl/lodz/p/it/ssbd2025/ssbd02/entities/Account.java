package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.TokenConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.UserRoleConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.validation.ValidLogin;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.validation.ValidIpAddress;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

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
    @NotBlank(message = AccountConsts.LOGIN_NOT_BLANK_MESSAGE)
    @Column(updatable = false, nullable = false, unique = true, length = AccountConsts.LOGIN_MAX)
    @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX, message = AccountConsts.LOGIN_SIZE_MESSAGE)
    @ValidLogin
    private String login;

    @NotBlank(message = AccountConsts.PASSWORD_NOT_BLANK_MESSAGE)
    @Column(name = AccountConsts.COLUMN_PASSWORD, updatable = true, nullable = false, length = AccountConsts.PASSWORD_MAX)
    @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX, message = AccountConsts.PASSWORD_SIZE_MESSAGE)
    @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE)
    @ToString.Exclude
    private String password;

    @NotBlank(message = AccountConsts.EMAIL_NOT_BLANK_MESSAGE)
    @Email(message = AccountConsts.EMAIL_INVALID_MESSAGE)
    @Column(name = AccountConsts.COLUMN_EMAIL, updatable = true, nullable = false, unique = true, length = AccountConsts.EMAIL_MAX)
    @Size(max = AccountConsts.EMAIL_MAX, message = AccountConsts.EMAIL_SIZE_MESSAGE)
    @Pattern(regexp = AccountConsts.EMAIL_REGEX, message = AccountConsts.EMAIL_MESSAGE)
    private String email;

    @NotNull(message = AccountConsts.ACTIVE_NOT_NULL_MESSAGE)
    @Column(updatable = true, nullable = false)
    private boolean active = AccountConsts.DEFAULT_ACTIVE;

    @Column(name = AccountConsts.COLUMN_LAST_SUCCESSFUL_LOGIN, updatable = true)
    @PastOrPresent(message = AccountConsts.LAST_SUCCESSFUL_LOGIN_PAST_OR_PRESENT)
    private Timestamp lastSuccessfulLogin;

    @Column(name = AccountConsts.COLUMN_LAST_FAILED_LOGIN, updatable = true)
    @PastOrPresent(message = AccountConsts.LAST_FAILED_LOGIN_PAST_OR_PRESENT)
    private Timestamp lastFailedLogin;

    @NotNull(message = AccountConsts.VERIFIED_NOT_NULL_MESSAGE)
    @Column(updatable = true, nullable = false)
    private boolean verified = AccountConsts.DEFAULT_VERIFIED;

    @Column(name = AccountConsts.COLUMN_LANGUAGE, updatable = true)
    private Language language;

    @Column(name = AccountConsts.COLUMN_LAST_SUCCESSFUL_LOGIN_IP, updatable = true, length = AccountConsts.IP_MAX)
    @Size(max = AccountConsts.IP_MAX, message = AccountConsts.IP_SIZE_MESSAGE)
    @ValidIpAddress(allowEmpty = true)
    private String lastSuccessfulLoginIp;

    @Column(name = AccountConsts.COLUMN_LAST_FAILED_LOGIN_IP, updatable = true, length = AccountConsts.IP_MAX)
    @Size(max = AccountConsts.IP_MAX, message = AccountConsts.IP_SIZE_MESSAGE)
    @ValidIpAddress(allowEmpty = true)
    private String lastFailedLoginIp;

    @OneToMany(mappedBy = UserRoleConsts.FIELD_ACCOUNT, cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE})
    @ToString.Exclude
    private Collection<UserRole> userRoles = new ArrayList<>();

    @NotBlank(message = AccountConsts.FIRST_NAME_NOT_BLANK_MESSAGE)
    @Column(name = "first_name", table = "user_data", updatable = true, nullable = false, length = AccountConsts.NAME_MAX)
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, message = AccountConsts.FIRST_NAME_SIZE_MESSAGE)
    @Pattern(regexp = AccountConsts.NAME_REGEX, message = AccountConsts.NAME_MESSAGE)
    private String firstName;

    @NotBlank(message = AccountConsts.LAST_NAME_NOT_BLANK_MESSAGE)
    @Column(name = "last_name", table = "user_data", updatable = true, nullable = false, length = AccountConsts.NAME_MAX)
    @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, message = AccountConsts.LAST_NAME_SIZE_MESSAGE)
    @Pattern(regexp = AccountConsts.NAME_REGEX, message = AccountConsts.NAME_MESSAGE)
    private String lastName;

    @NotNull(message = AccountConsts.TWO_FACTOR_AUTH_NOT_NULL_MESSAGE)
    @Column(name = AccountConsts.COLUMN_TWO_FACTOR_AUTH, updatable = true, nullable = false)
    private boolean twoFactorAuth = AccountConsts.DEFAULT_TWO_FACTOR_AUTH;

    @NotNull(message = AccountConsts.REMINDED_NOT_NULL_MESSAGE)
    @Column(name = AccountConsts.COLUMN_REMINDED, updatable = true, nullable = false)
    private boolean reminded = AccountConsts.DEFAULT_REMINDED;

    @NotNull(message = AccountConsts.LOGIN_ATTEMPTS_NOT_NULL_MESSAGE)
    @Min(value = 0, message = AccountConsts.LOGIN_ATTEMPTS_MIN_MESSAGE)
    @Max(value = AccountConsts.MAX_LOGIN_ATTEMPTS, message = AccountConsts.LOGIN_ATTEMPTS_MAX_MESSAGE)
    @Column(name = AccountConsts.COLUMN_LOGIN_ATTEMPTS, updatable = true, nullable = false)
    private int loginAttempts = AccountConsts.DEFAULT_LOGIN_ATTEMPTS;

    @Column(name = AccountConsts.COLUMN_LOCKED_UNTIL, updatable = true)
    @Future(message = AccountConsts.LOCKED_UNTIL_FUTURE_MESSAGE)
    private Timestamp lockedUntil;

    @NotNull(message = AccountConsts.AUTO_LOCKED_NOT_NULL_MESSAGE)
    @Column(name = AccountConsts.COLUMN_AUTO_LOCKED, updatable = true, nullable = false)
    private boolean autoLocked;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = AccountConsts.TABLE_NAME_PASSWORD_HISTORY,
            joinColumns = @JoinColumn(
                    name = TokenConsts.COLUMN_ACCOUNT_ID,
                    nullable = false,
                    insertable = false,
                    updatable = false
            ),
            uniqueConstraints = @UniqueConstraint(
                    columnNames = {TokenConsts.COLUMN_ACCOUNT_ID, AccountConsts.COLUMN_OLD_PASSWORD}
            )
    )
    @Column(
            name = AccountConsts.COLUMN_OLD_PASSWORD,
            nullable = false,
            insertable = false,
            updatable = false
    )
    @Size(
            max = AccountConsts.MAX_PASSWORD_HISTORY,
            message = AccountConsts.PASSWORD_HISTORY_SIZE_MESSAGE
    )
    @ToString.Exclude
    private Collection<String> previousPasswords = new ArrayList<>();
}