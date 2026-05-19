package ru.skypro.homework.service;

import org.springframework.stereotype.Service;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;

@Service
public class UserService {
    public final UserRepository userRepository;
    public final UserMapper userMapper;


    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;


    }
}
