package pl.lodz.p.it.ssbd2025.ssbd02.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Table(name = "account")
@SecondaryTable(name = "user_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AbstractEntity {

    @Column(updatable = false, nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "email", nullable = false, unique = true, length = 60)
    private String email;

    @Column(nullable = false)
    private boolean active = false;

    @Column(name = "last_successful_login")
    private Timestamp lastSuccessfulLogin;

    @Column(name = "last_failed_login")
    private Timestamp lastFailedLogin;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(name = "language")
    private Language language;

    @Column(name = "last_successful_login_ip", length = 45)
    private String lastSuccessfulLoginIp;

    @Column(name = "last_failed_login_ip", length = 45)
    private String lastFailedLoginIp;

    @OneToMany(mappedBy = "account", cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<UserRole> userRoles = new ArrayList<>();

    @Column(name = "first_name", table = "user_data", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", table = "user_data", nullable = false, length = 50)
    private String lastName;
}
