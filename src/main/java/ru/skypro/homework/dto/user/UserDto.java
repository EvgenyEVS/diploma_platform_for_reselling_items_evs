package ru.skypro.homework.dto.user;

import lombok.Data;
import ru.skypro.homework.dto.Role;

@Data
public class UserDto {
    Integer id;
    String email;
    String firstName;
    String lastName;
    String phone;
    Role role;
    String image;
}
