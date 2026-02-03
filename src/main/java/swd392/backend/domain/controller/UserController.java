package swd392.backend.domain.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import swd392.backend.domain.dto.UserDTO;
import swd392.backend.domain.service.user.UserService;

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

    @GetMapping("/{id}")
    public UserDTO findUserById(@PathVariable Integer id) {
        return userService.findUserById(id);
    }
}
