package com.bahubba.bahubbabookclub.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerTest {
    @Autowired
    UserController userController;

    @MockBean
    UserService userService;

    @Test
    void testGetByID() {
        when(userService.findByID(any(UUID.class))).thenReturn(new UserDTO());
        ResponseEntity<UserDTO> rsp = userController.getByID(UUID.randomUUID());
        verify(userService, times(1)).findByID(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testGetAll() {
        when(userService.findAll()).thenReturn(new ArrayList<>());
        ResponseEntity<List<UserDTO>> rsp = userController.getAll();
        verify(userService, times(1)).findAll();
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }

    @Test
    void testRemoveUser() {
        when(userService.removeUser(any(UUID.class))).thenReturn(new UserDTO());
        ResponseEntity<UserDTO> rsp = userController.removeUser(UUID.randomUUID());
        verify(userService, times(1)).removeUser(any(UUID.class));
        assertThat(rsp).isNotNull();
        assertThat(rsp.getBody()).isNotNull();
    }
}
