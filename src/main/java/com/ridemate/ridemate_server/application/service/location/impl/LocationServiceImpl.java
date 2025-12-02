package com.ridemate.ridemate_server.application.service.location.impl;

import com.ridemate.ridemate_server.application.dto.location.LocationResponse;
import com.ridemate.ridemate_server.application.dto.location.SaveLocationRequest;
import com.ridemate.ridemate_server.application.mapper.LocationMapper;
import com.ridemate.ridemate_server.application.service.location.LocationService;
import com.ridemate.ridemate_server.domain.entity.FavoriteLocation;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.repository.FavoriteLocationRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.presentation.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {

    @Autowired
    private FavoriteLocationRepository locationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LocationMapper locationMapper;

    @Override
    @Transactional
    public LocationResponse saveLocation(Long userId, SaveLocationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra trùng tên địa điểm (Ví dụ: không thể lưu 2 cái tên "Nhà")
        if (locationRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new IllegalArgumentException("Location with this name already exists");
        }

        FavoriteLocation location = FavoriteLocation.builder()
                .user(user)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        location = locationRepository.save(location);
        return locationMapper.toResponse(location);
    }

    @Override
    public List<LocationResponse> getMyLocations(Long userId) {
        List<FavoriteLocation> locations = locationRepository.findByUserId(userId);
        return locations.stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLocation(Long userId, Long locationId) {
        FavoriteLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (!location.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to delete this location");
        }

        locationRepository.delete(location);
    }
}