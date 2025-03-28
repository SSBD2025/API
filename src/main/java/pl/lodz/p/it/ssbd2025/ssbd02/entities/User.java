package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, length = 60)
    private String email;

    @Column(name = "password", nullable = false, length = 60)
    private String password;

    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    //TODO potem zamienic
    private UUID surveyId;

    //TODO potem zamienic
    private UUID testResultsId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserBloodTestReport> bloodTestReports;

    //TODO potem zamienic
    private UUID dietProfileId;

//    @OneToOne
//    @JoinColumn(name = "dietary_restrictions_id")
//    private DietaryRestrictions dietaryRestrictions;
}
