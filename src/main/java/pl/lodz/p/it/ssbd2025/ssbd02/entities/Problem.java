package pl.lodz.p.it.ssbd2025.ssbd02.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.User;
import pl.lodz.p.it.ssbd2025.ssbd02.entities.DietProfile;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Length(min = 1, max = 255)
    private String description;
    private Timestamp date;
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//    @ManyToOne
//    @JoinColumn(name = "diet_profile_id", nullable = false)
//    private DietProfile dietProfile;
}
