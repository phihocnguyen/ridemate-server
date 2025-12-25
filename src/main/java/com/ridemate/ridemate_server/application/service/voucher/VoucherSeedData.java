package com.ridemate.ridemate_server.application.service.voucher;

import com.ridemate.ridemate_server.domain.entity.Voucher;
import com.ridemate.ridemate_server.domain.repository.VoucherRepository;
import com.ridemate.ridemate_server.domain.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Seed data for Vouchers
 * This will run only in development/test profile
 * To use: add --spring.profiles.active=dev to your application.properties or run command
 */
@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class VoucherSeedData {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Bean
    @Transactional
    CommandLineRunner initVoucherData() {
        return args -> {
            // Delete all existing user vouchers first (to avoid foreign key constraint)
            long userVoucherCount = userVoucherRepository.count();
            if (userVoucherCount > 0) {
                log.info("Deleting {} existing user vouchers...", userVoucherCount);
                userVoucherRepository.deleteAll();
            }

            // Delete all existing vouchers
            long voucherCount = voucherRepository.count();
            if (voucherCount > 0) {
                log.info("Deleting {} existing vouchers...", voucherCount);
                voucherRepository.deleteAll();
            }

            log.info("Seeding new vouchers...");

            // Set expiry date to 90 days from now
            LocalDateTime expiryDate = LocalDateTime.now().plusDays(90);

            List<Voucher> vouchers = Arrays.asList(
                // Food & Beverage Vouchers
                Voucher.builder()
                    .voucherCode("STARBUCKS-50K")
                    .description("Voucher Starbucks 50.000đ - Đổi 1 ly cà phê bất kỳ")
                    .voucherType(Voucher.VoucherType.FOOD_AND_BEVERAGE)
                    .cost(500)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("MCDONALD-100K")
                    .description("Voucher McDonald's 100.000đ - Combo Big Mac")
                    .voucherType(Voucher.VoucherType.FOOD_AND_BEVERAGE)
                    .cost(800)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("COCA-COLA-30K")
                    .description("Voucher Coca-Cola 30.000đ - 1 thùng nước ngọt")
                    .voucherType(Voucher.VoucherType.FOOD_AND_BEVERAGE)
                    .cost(300)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("KFC-150K")
                    .description("Voucher KFC 150.000đ - Combo gà rán")
                    .voucherType(Voucher.VoucherType.FOOD_AND_BEVERAGE)
                    .cost(1200)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("LOTTERIA-80K")
                    .description("Voucher Lotteria 80.000đ - Combo burger")
                    .voucherType(Voucher.VoucherType.FOOD_AND_BEVERAGE)
                    .cost(600)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                // Shopping Vouchers
                Voucher.builder()
                    .voucherCode("SHOPEE-200K")
                    .description("Voucher Shopee 200.000đ - Giảm giá đơn hàng")
                    .voucherType(Voucher.VoucherType.SHOPPING)
                    .cost(1500)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("LAZADA-150K")
                    .description("Voucher Lazada 150.000đ - Giảm giá đơn hàng")
                    .voucherType(Voucher.VoucherType.SHOPPING)
                    .cost(1200)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("TIKI-100K")
                    .description("Voucher Tiki 100.000đ - Giảm giá đơn hàng")
                    .voucherType(Voucher.VoucherType.SHOPPING)
                    .cost(800)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("SENDO-80K")
                    .description("Voucher Sendo 80.000đ - Giảm giá đơn hàng")
                    .voucherType(Voucher.VoucherType.SHOPPING)
                    .cost(600)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                // Vehicle Service Vouchers
                Voucher.builder()
                    .voucherCode("VINFAST-500K")
                    .description("Voucher VinFast 500.000đ - Dịch vụ bảo dưỡng xe")
                    .voucherType(Voucher.VoucherType.VEHICLE_SERVICE)
                    .cost(3000)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("HONDA-300K")
                    .description("Voucher Honda 300.000đ - Dịch vụ bảo dưỡng xe")
                    .voucherType(Voucher.VoucherType.VEHICLE_SERVICE)
                    .cost(2000)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("YAMAHA-250K")
                    .description("Voucher Yamaha 250.000đ - Dịch vụ bảo dưỡng xe")
                    .voucherType(Voucher.VoucherType.VEHICLE_SERVICE)
                    .cost(1800)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build(),

                Voucher.builder()
                    .voucherCode("PETROL-200K")
                    .description("Voucher Xăng dầu 200.000đ - Nạp xăng")
                    .voucherType(Voucher.VoucherType.VEHICLE_SERVICE)
                    .cost(1500)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .build()
            );

            voucherRepository.saveAll(vouchers);
            log.info("Successfully seeded {} vouchers", vouchers.size());
        };
    }
}

