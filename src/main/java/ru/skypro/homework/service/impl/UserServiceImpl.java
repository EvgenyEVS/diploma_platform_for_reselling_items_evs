package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.skypro.homework.dto.user.NewPasswordDto;
import ru.skypro.homework.dto.user.UpdateUserDto;
import ru.skypro.homework.dto.user.UserDto;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements ru.skypro.homework.service.UserService {
    public final UserRepository userRepository;
    public final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = "uploads/images/avatars/";


    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }


    @Override
    public UserDto getUser(String username) {
        log.debug("Getting user DTO by username: {}", username);
        User user = findByUsername(username);
        return userMapper.toUserDto(user);
    }


    @Override
    @PreAuthorize("#username == authentication.name")
    public void updateUserPassword(String username, NewPasswordDto newPasswordDto) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(newPasswordDto.getCurrentPassword(), user.getPassword())) {
            log.warn("Invalid current password for user: {}", username);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPasswordDto.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", username);
    }


    @Override
    @PreAuthorize("#username == authentication.name")
    public UpdateUserDto updateUser(String username, UpdateUserDto updateUserDto) {
        log.debug("Updating user for: {}", username);

        User user = findByUsername(username);

        if (updateUserDto.getFirstName() != null) {
            user.setFirstName(updateUserDto.getFirstName());
        }

        if (updateUserDto.getLastName() != null) {
            user.setLastName(updateUserDto.getLastName());
        }

        if (updateUserDto.getPhone() != null) {
            user.setPhone(updateUserDto.getPhone());
        }

        User updateUser = userRepository.save(user);
        log.info("User info updated successfully for: {}", username);
        return userMapper.toUpdateUserDto(updateUser);
    }


    @Override
    @PreAuthorize("#username == authentication.name")
    public String updateUserImage(String username, MultipartFile image) {
        log.debug("Updating avatar for user: {}", username);

        User user = findByUsername(username);

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            deleteImage(user.getImage());
        }

        String imagePath = saveImage(image);
        user.setImage(imagePath);
        userRepository.save(user);

        log.info("Avatar updated successfully for user: {}", username);
        return imagePath;
    }


    @Override
    public byte[] getUserImage(String username) {
        log.debug("Getting avatar for user: {}", username);

        User user = findByUsername(username);
        if (user.getImage() == null || user.getImage().isEmpty()) {
            log.debug("User {} has no avatar", username);
            return new byte[0];
        }

        Path path = Paths.get(UPLOAD_DIR, user.getImage());

        try {
            if (Files.exists(path)) {
                byte[] imageBytes = Files.readAllBytes(path);
                log.debug("Successfully loaded avatar for user: {}, size: {} bytes", username, imageBytes.length);
                return imageBytes;
            } else {
                log.warn("Avatar file not found for user: {}, path: {}", username, path);
                return new byte[0];
            }
        } catch (IOException e) {
            log.error("Failed to read avatar for user: {}", username, e);
            return new byte[0];
        }
    }


    //private methods

    private String saveImage(MultipartFile image) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFileName = image.getOriginalFilename();
            String extensions = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extensions = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + extensions;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Saved image for user: {}", fileName);
            return fileName;

        } catch (IOException e) {
            log.error("Failed to save image", e);
            throw new RuntimeException("Failed to save image", e);
        }
    }


    private void deleteImage(String imagePath) {
        try {
            Path path = Paths.get(UPLOAD_DIR, imagePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.debug("Deleted image file: {}", imagePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete image file: {}", imagePath, e);
        }
    }

}
