package com.bahubba.bahubbabookclub.controller;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** User endpoints */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Controller", description = "User endpoints")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieve a user by ID
     *
     * @param id The user ID
     * @return The user info
     * @throws UserNotFoundException The user was not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieves a user by ID")
    public ResponseEntity<UserDTO> getByID(@PathVariable UUID id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.findByID(id));
    }

    /**
     * Retrieves all users (users)
     *
     * @return All users (users)
     */
    @GetMapping("/all")
    @Operation(summary = "Get All Users", description = "Retrieves all users (users)")
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    /**
     * Removes (soft deletes) user
     *
     * @param id The user ID
     * @return Persisted data from the soft deleted user
     * @throws UserNotFoundException The user was not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove User", description = "Removes (soft deletes) a user")
    public ResponseEntity<UserDTO> removeUser(@PathVariable UUID id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.removeUser(id));
    }
}
