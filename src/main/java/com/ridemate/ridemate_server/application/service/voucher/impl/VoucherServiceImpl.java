package com.ridemate.ridemate_server.application.service.voucher.impl;

import com.ridemate.ridemate_server.application.dto.voucher.UserVoucherDto;
import com.ridemate.ridemate_server.application.dto.voucher.VoucherDto;
import com.ridemate.ridemate_server.application.service.voucher.VoucherService;
import com.ridemate.ridemate_server.domain.entity.User;
import com.ridemate.ridemate_server.domain.entity.UserVoucher;
import com.ridemate.ridemate_server.domain.entity.Voucher;
import com.ridemate.ridemate_server.domain.repository.UserVoucherRepository;
import com.ridemate.ridemate_server.domain.repository.UserRepository;
import com.ridemate.ridemate_server.domain.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VoucherDto createVoucher(VoucherDto voucherDto) {
        Voucher voucher = Voucher.builder()
                .voucherCode(voucherDto.getVoucherCode())
                .description(voucherDto.getDescription())
                .voucherType(voucherDto.getVoucherType())
                .cost(voucherDto.getCost())
                .expiryDate(voucherDto.getExpiryDate())
                .isActive(true)
                .build();

        voucher = voucherRepository.save(voucher);
        return mapToVoucherDto(voucher);
    }

    @Override
    public List<VoucherDto> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .filter(Voucher::getIsActive)
                .map(this::mapToVoucherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserVoucherDto redeemVoucher(Long userId, Long voucherId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found"));

        if (!voucher.getIsActive()) {
            throw new IllegalArgumentException("Voucher is not active");
        }

        if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Voucher has expired");
        }

        if (user.getCoins() < voucher.getCost()) {
            throw new IllegalArgumentException("Insufficient coins");
        }

        // Deduct coins
        user.setCoins(user.getCoins() - voucher.getCost());
        userRepository.save(user);

        // Create UserVoucher
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .status(UserVoucher.UserVoucherStatus.UNUSED)
                .acquiredDate(LocalDateTime.now())
                .build();

        userVoucher = userVoucherRepository.save(userVoucher);

        return mapToUserVoucherDto(userVoucher);
    }

    @Override
    public List<UserVoucherDto> getUserVouchers(Long userId) {
        return userVoucherRepository.findByUserId(userId).stream()
                .map(this::mapToUserVoucherDto)
                .collect(Collectors.toList());
    }

    private VoucherDto mapToVoucherDto(Voucher voucher) {
        return VoucherDto.builder()
                .id(voucher.getId())
                .voucherCode(voucher.getVoucherCode())
                .description(voucher.getDescription())
                .voucherType(voucher.getVoucherType())
                .cost(voucher.getCost())
                .expiryDate(voucher.getExpiryDate())
                .isActive(voucher.getIsActive())
                .build();
    }

    private UserVoucherDto mapToUserVoucherDto(UserVoucher userVoucher) {
        return UserVoucherDto.builder()
                .id(userVoucher.getId())
                .voucher(mapToVoucherDto(userVoucher.getVoucher()))
                .status(userVoucher.getStatus())
                .acquiredDate(userVoucher.getAcquiredDate())
                .build();
    }
}
