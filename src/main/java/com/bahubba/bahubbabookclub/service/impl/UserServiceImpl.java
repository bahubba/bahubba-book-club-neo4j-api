package com.bahubba.bahubbabookclub.service.impl;

import com.bahubba.bahubbabookclub.exception.UserNotFoundException;
import com.bahubba.bahubbabookclub.model.dto.UserDTO;
import com.bahubba.bahubbabookclub.model.entity.User;
import com.bahubba.bahubbabookclub.model.mapper.UserMapper;
import com.bahubba.bahubbabookclub.repository.UserRepo;
import com.bahubba.bahubbabookclub.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public UserDTO findByID(UUID id) throws UserNotFoundException {
        return userMapper.entityToDTO(userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }

    // TODO - Convert to pagination
    @Override
    public List<UserDTO> findAll() {
        return userMapper.entityListToDTO(userRepo.findAll());
    }

    // FIXME - Need to ensure the user is removing themself
    @Override
    public UserDTO removeUser(UUID id) throws UserNotFoundException {
        User user = userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setDeparted(LocalDateTime.now());
        return userMapper.entityToDTO(userRepo.save(user));
    }
}
