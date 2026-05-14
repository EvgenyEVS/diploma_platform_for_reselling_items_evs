package ru.skypro.homework.dto.ads;

import lombok.Data;

@Data
public class ExtendedAdDto {

    int pk;
    String authorFirstName;
    String authorLastName;
    String description;
    String email;
    String image;
    String phone;
    int price;
    String title;
}
