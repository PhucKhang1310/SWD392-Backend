package swd392.backend.domain.service.user;

import swd392.backend.domain.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAllUsers();
    UserDTO findUserById(Integer id);
}
