package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.consts.TokenConsts;

import java.util.Date;

@Entity
@Table(name = TokenConsts.TABLE_NAME)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
public class TokenEntity extends AbstractEntity {

    @Column(name = TokenConsts.COLUMN_TOKEN, unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    @NotNull
    @NotBlank
    @ToString.Exclude
    private String token;

    @Future
    @Column(name = TokenConsts.COLUMN_EXPIRATION, nullable = false, updatable = false, unique = false)
    private Date expiration;

    @NotNull
    @ManyToOne
    @JoinColumn(name = TokenConsts.COLUMN_ACCOUNT_ID, nullable = false, updatable = false, unique = false)
    private Account account;

    @NotNull
    @Column(name = TokenConsts.COLUMN_TYPE, nullable = false, updatable = false, unique = false)
    private TokenType type;
}
