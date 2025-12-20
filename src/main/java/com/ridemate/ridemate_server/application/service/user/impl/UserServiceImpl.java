package com.ridemate.ridemate_server.application.service.user.impl;

import com.ridemate.ridemate_server.application.dto.user.UpdateDriverStatusRequest;
import com.ridemate.ridemate_server.application.dto.user.UpdateProfileRequest;
import com.ridemate.ridemate_server.application.dto.user.UserDto;
import com.ridemate.ridemate_server.application.mapper.UserMapper;
import com.ridemate.ridemate_server.application.service.user.UserService;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return getUserProfile(userId);
    }

    @Override
    @Transactional
    public UserDto updateDriverStatus(Long userId, UpdateDriverStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Only drivers can update driver status
        if (user.getUserType() != User.UserType.DRIVER) {
            throw new IllegalArgumentException("Only drivers can update driver status");
        }

        // Parse and validate status
        User.DriverStatus newStatus;
        try {
            newStatus = User.DriverStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid driver status: " + request.getStatus() + 
                    ". Valid values are: ONLINE, OFFLINE, BUSY");
        }

        user.setDriverStatus(newStatus);

        // Update location if provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            user.setCurrentLatitude(request.getLatitude());
            user.setCurrentLongitude(request.getLongitude());
            user.setLastLocationUpdate(LocalDateTime.now());
        }

        user = userRepository.save(user);
        
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update fields if provided (only fields that exist in User entity)
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        // Note: dob, address, bankName, bankAccountNumber are not in User entity
        // They might be in a separate Profile entity or not implemented yet

        user = userRepository.save(user);
        
        return userMapper.toUserDto(user);
    }
}