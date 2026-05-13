package ru.skypro.homework.dto.user;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private String phone;
}
