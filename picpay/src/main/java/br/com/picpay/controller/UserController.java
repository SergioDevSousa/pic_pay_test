package br.com.picpay.controller;

import br.com.picpay.dto.request.UserRequest;
import br.com.picpay.dto.response.UserResponse;
import br.com.picpay.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { 
        this.userService = userService; 
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) { return userService.create(request); }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) { return userService.findById(id); }

    @GetMapping
    public List<UserResponse> findAll() { return userService.findAll(); }
}
