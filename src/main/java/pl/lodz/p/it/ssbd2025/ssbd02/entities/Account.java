package pl.lodz.p.it.ssbd2025.ssbd02.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(nullable = false)
    private boolean active;

    @OneToMany
    @Column(name = "user_role", nullable = false)
    private Collection<UserRole> userRoles = new ArrayList<>();

    @Column(name = "first_name", table = "user_data", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", table = "user_data", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", table = "user_data", nullable = false, length = 60)
    private String email;

    @Column(name = "phone_number", table = "user_data", nullable = false)
    private String phoneNumber;
}
