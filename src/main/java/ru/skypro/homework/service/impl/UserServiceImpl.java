package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

@Service
public class UserServiceImpl implements ru.skypro.homework.service.UserService {
    public final UserRepository userRepository;
    public final UserMapper userMapper;


    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;


    }


    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
