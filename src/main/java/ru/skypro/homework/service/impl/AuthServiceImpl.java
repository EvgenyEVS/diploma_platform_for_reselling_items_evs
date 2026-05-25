package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.model.User;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
    }

    @Override
    public boolean login(String userName, String password) {
        log.debug("Login attempt for user: {}", userName);

        try {
            UserDetails userDetails = loadUserByUsername(userName);
            boolean matches = encoder.matches(password, userDetails.getPassword());

            if (matches) {
                log.info("Successful login for user: {}", userName);
            } else {
                log.info("Failed login for user: {} - invalid password", userName);
            }
            return matches;

        } catch (UsernameNotFoundException e) {
            log.warn("Failed login for user: {} - user not found", userName);
            return false;
        }
    }

    @Override
    public boolean register(Register register) {
        log.debug("Registration attempt for user: {}", register.getUsername());

        if (userRepository.findByUsername(register.getUsername()).isPresent()) {
            log.warn("Registration failed - user already exists: {}", register.getUsername());
            return false;
        }

        User user = new User();
        user.setUsername(register.getUsername());
        user.setPassword(encoder.encode(register.getPassword()));
        user.setFirstName(register.getFirstName());
        user.setLastName(register.getLastName());
        user.setPhone(register.getPhone());
        user.setRole(register.getRole() != null ? register.getRole() : Role.USER);
        user.setImage(null);

        try {
            userRepository.save(user);
            log.info("Successfully registered user: {}", register.getUsername());
            return true;
        } catch (Exception e) {
            log.error("Registration failed for user: {}", register.getUsername());
            return false;
        }


    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: {}" + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
