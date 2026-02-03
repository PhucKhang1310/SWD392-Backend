package swd392.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthResponseDTO {
    Integer id;
    String email;
    String fullName;
    String role;
    String message;

    public static AuthResponseDTO success(Integer id, String email, String fullName, String role) {
        return new AuthResponseDTO(id, email, fullName, role, "Success");
    }

    public static AuthResponseDTO error(String message) {
        return new AuthResponseDTO(null, null, null, null, message);
    }
}
