package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.ssbd2025.ssbd02.enums.TokenType;
import pl.lodz.p.it.ssbd2025.ssbd02.utils.JwtTokenProvider;

import java.util.Date;

@Entity
@Table(name = "token_entity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenEntity extends AbstractEntity {

    @Column(name = "token", unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;

    @Future
    @Column(name = "expiration",nullable = false, updatable = false)
    private Date expiration;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    @Column(name = "type", nullable = false, updatable = false)
    private TokenType type;


}
