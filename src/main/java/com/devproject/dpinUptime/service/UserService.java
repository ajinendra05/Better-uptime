package com.devproject.dpinUptime.service;

import com.devproject.dpinUptime.DTO.UserDto;
import com.devproject.dpinUptime.exception.EmailExistsException;

public interface UserService {
    void createUser(UserDto userDto) throws EmailExistsException;
}