package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "jwt_entity")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JwtEntity extends AbstractEntity {

    @Column(name = "token", unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;

    @Future
    @Column(name = "expiration",nullable = false, updatable = false)
    private Date expiration;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;
}
