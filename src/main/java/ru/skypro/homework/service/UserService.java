package ru.skypro.homework.service;

import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.user.NewPasswordDto;
import ru.skypro.homework.dto.user.UpdateUserDto;
import ru.skypro.homework.dto.user.UserDto;
import ru.skypro.homework.model.User;

public interface UserService {

    User findByUsername(String username);

    UserDto getUser(String username);

    void updateUserPassword (String username, NewPasswordDto newPasswordDto);

    UpdateUserDto updateUser (String username, UpdateUserDto updateUserDto);

    String updateUserImage (String username, MultipartFile image);

    byte[] getUserImage(String username);
}
