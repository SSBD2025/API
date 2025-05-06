package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "password_reset_token_entity")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PasswordResetToken extends AbstractEntity {

    @Column(name = "token", unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;

    @Future
    @Column(name = "expiration",nullable = false, updatable = false)
    private Date expiration;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    public PasswordResetToken(String token, Account account) {
        this.token = token;
        this.account = account;
        this.expiration = getTokenExpirationTime();
    }

    private Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.MINUTE, 5);
        return new Date(calendar.getTime().getTime());
    }

}
