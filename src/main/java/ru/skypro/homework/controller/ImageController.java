package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.AdService;
import ru.skypro.homework.service.UserService;

@RestController
public class ImageController {

    private final AdService adService;
    private final UserService userService;

    public ImageController(AdService adService, UserService userService) {
        this.adService = adService;
        this.userService = userService;
    }


    @GetMapping(value = "/images/ads/{id}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    public ResponseEntity<byte[]> getAdImage(@PathVariable int id) {
        byte[] image = adService.getAdImage(id);
        if (image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }


    @GetMapping(value = "/images/avatars/{username}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    public ResponseEntity<byte[]> getUserImage(@PathVariable String username) {
        byte[] image = userService.getUserImage(username);
        if (image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(image);
    }
}