package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
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
    @ToString.Exclude
    private String token;

    @Future
    @Column(name = TokenConsts.COLUMN_EXPIRATION, nullable = false, updatable = false)
    private Date expiration;

    @ManyToOne
    @JoinColumn(name = TokenConsts.COLUMN_ACCOUNT_ID, nullable = false, updatable = false)
    private Account account;

    @Column(name = TokenConsts.COLUMN_TYPE, nullable = false, updatable = false)
    private TokenType type;
}
