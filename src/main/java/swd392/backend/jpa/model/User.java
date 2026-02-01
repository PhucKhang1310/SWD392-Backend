package swd392.backend.jpa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 255)
    @Nationalized
    @Column(name = "full_name")
    private String fullName;

    @Size(max = 20)
    @NotNull
    @Nationalized
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Size(max = 50)
    @Nationalized
    @Column(name = "status", length = 50)
    private String status;

}