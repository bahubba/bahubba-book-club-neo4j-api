package com.bahubba.bahubbabookclub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    UserService userService;

    @MockBean
    UserRepo userRepo;

    @Test
    void testFindByID() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
        UserDTO result = userService.findByID(UUID.randomUUID());
        verify(userRepo, times(1)).findById(any(UUID.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testFindByID_NotFound() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.findByID(UUID.randomUUID()));
    }

    @Test
    void testFindAll() {
        when(userRepo.findAll()).thenReturn(new ArrayList<>(List.of(new User())));
        List<UserDTO> result = userService.findAll();
        verify(userRepo, times(1)).findAll();
        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void testRemoveUser() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.of(new User()));
        when(userRepo.save(any(User.class))).thenReturn(new User());
        UserDTO result = userService.removeUser(UUID.randomUUID());
        verify(userRepo, times(1)).findById(any(UUID.class));
        verify(userRepo, times(1)).save(any(User.class));
        assertThat(result).isNotNull();
    }

    @Test
    void testRemoveUser_UserNotFound() {
        when(userRepo.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.removeUser(UUID.randomUUID()));
    }
}
