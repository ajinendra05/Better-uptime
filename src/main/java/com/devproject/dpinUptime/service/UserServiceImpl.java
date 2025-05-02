package com.devproject.dpinUptime.service;

import com.devproject.dpinUptime.DTO.UserDto;
import com.devproject.dpinUptime.exception.EmailExistsException;
import com.devproject.dpinUptime.model.User;
import com.devproject.dpinUptime.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(UserDto userDto) throws EmailExistsException {
        String email = userDto.getEmail().toLowerCase();

        if (emailExists(email)) {
            throw new EmailExistsException("Email already registered: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRoles("ROLE_USER");
        userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}