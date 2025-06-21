package pl.lodz.p.it.ssbd2025.ssbd02.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.*;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnCreate;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnRead;
import pl.lodz.p.it.ssbd2025.ssbd02.dto.vgroups.OnUpdate;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.Language;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.AccountConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.DTOConsts;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.validation.ValidIpAddress;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.validation.ValidLogin;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Setter(AccessLevel.NONE)
@Getter
@AllArgsConstructor
public class AccountDTO {

        @Null(groups = {OnCreate.class, OnUpdate.class})
        @NotNull(groups = OnRead.class)
        UUID id;

        @Null(groups = {OnCreate.class, OnRead.class})
        @NotNull(groups = OnUpdate.class)
        Long version;

        @NotNull(groups = {OnCreate.class, OnRead.class})
        @Null(groups = OnUpdate.class)
        @Size(min = AccountConsts.LOGIN_MIN, max = AccountConsts.LOGIN_MAX, groups = OnCreate.class)
        @ValidLogin(groups = OnCreate.class)
        String login;

        @Null(groups = {OnUpdate.class, OnRead.class})
        @NotNull(groups = OnCreate.class)
        @Size(min = AccountConsts.PASSWORD_MIN, max = AccountConsts.PASSWORD_MAX, groups = OnCreate.class)
        @Pattern(regexp = AccountConsts.PASSWORD_REGEX, message = AccountConsts.PASSWORD_MESSAGE, groups = {OnCreate.class})
        String password;

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Boolean verified;

        @Null(groups = {OnCreate.class, OnUpdate.class, OnRead.class})
        Boolean active;

        @NotBlank(groups = {OnCreate.class, OnRead.class})
        @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
        @Pattern(regexp = AccountConsts.NAME_REGEX, message = AccountConsts.NAME_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
        String firstName;

        @NotBlank(groups = {OnCreate.class, OnRead.class})
        @Size(min = AccountConsts.NAME_MIN, max = AccountConsts.NAME_MAX, groups = {OnCreate.class, OnUpdate.class})
        @Pattern(regexp = AccountConsts.NAME_REGEX, message = AccountConsts.NAME_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
        String lastName;

        @NotNull(groups = {OnCreate.class, OnRead.class})
        @NotBlank(groups = OnCreate.class)
        @Email(groups = OnCreate.class)
        @Size(max = AccountConsts.EMAIL_MAX, groups = {OnCreate.class, OnUpdate.class})
        @Pattern(regexp = AccountConsts.EMAIL_REGEX, message = AccountConsts.EMAIL_MESSAGE, groups = {OnCreate.class, OnUpdate.class})
        String email;

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastSuccessfulLogin;

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        Timestamp lastFailedLogin;

        @NotNull(groups = OnRead.class)
        @NotNull(groups = {OnCreate.class, OnUpdate.class})
        Language language;

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = AccountConsts.IP_MAX, groups = OnRead.class)
        @ValidIpAddress(allowEmpty = true, groups = OnRead.class)
        String lastSuccessfulLoginIp;

        @NotNull(groups = OnRead.class)
        @Null(groups = {OnCreate.class, OnUpdate.class})
        @Size(max = AccountConsts.IP_MAX, groups = OnRead.class)
        @ValidIpAddress(allowEmpty = true, groups = OnRead.class)
        String lastFailedLoginIp;

        @NotNull(groups = {OnRead.class, OnCreate.class, OnUpdate.class})
        boolean twoFactorAuth;

        @NotNull(groups = {OnCreate.class})
        boolean reminded;

        @NotNull(groups = {OnCreate.class})
        @Min(value = 0, groups = {OnCreate.class})
        @Max(value = AccountConsts.MAX_LOGIN_ATTEMPTS, groups = {OnCreate.class})
        int loginAttempts;

        @Null(groups = {OnCreate.class})
        @Future(message = AccountConsts.LOCKED_UNTIL_FUTURE_MESSAGE, groups = OnCreate.class)
        Timestamp lockedUntil;

        @NotNull(groups = {OnCreate.class})
        boolean autoLocked;

        private AccountDTO() {}

        @Override
        public String toString() {
                return "AccountDTO{" +
                        "id=" + id +
                        ", version=" + version +
                        ", login='" + login + '\'' +
                        ", password='" + DTOConsts.PROTECTED + '\'' +
                        ", verified=" + verified +
                        ", active=" + active +
                        ", firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", email='" + email + '\'' +
                        ", lastSuccessfulLogin=" + lastSuccessfulLogin +
                        ", lastFailedLogin=" + lastFailedLogin +
                        ", language=" + language +
                        ", lastSuccessfulLoginIp='" + lastSuccessfulLoginIp + '\'' +
                        ", lastFailedLoginIp='" + lastFailedLoginIp + '\'' +
                        ", twoFactorAuth=" + twoFactorAuth +
                        ", reminded=" + reminded +
                        ", loginAttempts=" + loginAttempts +
                        ", lockedUntil=" + lockedUntil +
                        '}';
        }
}
