package swd392.backend.domain.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import swd392.backend.domain.dto.UserDTO;
import swd392.backend.domain.service.user.UserService;
import swd392.backend.jpa.model.User;
import swd392.backend.jpa.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDTO> findAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping
    public UserDTO findUserById(Long id) {
        return userService.findUserById(id);
    }
}
