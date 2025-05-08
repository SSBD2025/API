package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "verification_token")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VerificationToken extends AbstractEntity {
    @Column(name = "token", unique = true, nullable = false, updatable = false, columnDefinition = "TEXT")
    private String token;

    @Future
    @Column(name = "expiration",nullable = false, updatable = false)
    private Date expiration;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Account account;

    public VerificationToken(String token, Account account) {
        this.token = token;
        this.account = account;
        this.expiration = getTokenExpirationTime();
    }

    private Date getTokenExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(new Date().getTime());
        calendar.add(Calendar.HOUR, 24);
        return new Date(calendar.getTime().getTime());
    }
}
