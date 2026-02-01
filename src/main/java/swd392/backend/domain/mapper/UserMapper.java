package swd392.backend.domain.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import swd392.backend.domain.dto.UserDTO;
import swd392.backend.jpa.model.User;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto (User user);
    User toEntity (UserDTO userDTO);
}
