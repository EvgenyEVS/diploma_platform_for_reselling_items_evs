package ru.skypro.homework.controller;


import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.user.NewPasswordDto;
import ru.skypro.homework.dto.user.UpdateUserDto;
import ru.skypro.homework.dto.user.UserDto;


@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "Получение информации об авторизованном пользователе")
    public UserDto getUser() {
        return new UserDto();
    }

    @PostMapping("/set_password")
    @Operation(summary = "Обновление пароля")
    public void setPassword(@RequestBody NewPasswordDto newPasswordDto) {
    }

    @PatchMapping("/me")
    @Operation(summary = "Обновление информации об авторизованном пользователе")
    public UpdateUserDto updateUser(@RequestBody UpdateUserDto updateUserDto) {
        return updateUserDto;
    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновление аватара авторизованного пользователя")
    public void updateUserImage(@RequestParam("image") MultipartFile image) {
    }

}
