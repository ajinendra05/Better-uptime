// package com.devproject.dpinUptime.service;
// import com.devproject.dpinUptime.DTO.UserDto;
// import com.devproject.dpinUptime.exception.EmailExistsException;
// import com.devproject.dpinUptime.model.User;
// import com.devproject.dpinUptime.repository.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import java.util.Optional;
// import java.util.List;

// @Service
// public class UserServiceImpl implements UserService {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @Override
//     public void createUser(UserDto userDto) throws EmailExistsException {
//         if (emailExists(userDto.getEmail())) {
//             throw new EmailExistsException("Email already exists");
//         }
        
//         User user = new User();
//         user.setEmail(userDto.getEmail());
//         user.setPassword(passwordEncoder.encode(userDto.getPassword()));
//         user.setRoles("ROLE_USER");
//         userRepository.save(user);
//     }

//     private boolean emailExists(String email) {
//         return userRepository.findByEmail(email) != null;
//     }
// }
