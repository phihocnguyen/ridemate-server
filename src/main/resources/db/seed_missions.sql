-- Seed data for Missions
-- Run this script to populate initial missions

-- 1. Daily Mission: Complete 1 trip
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Hoàn thành chuyến đi đầu tiên',
    'Hoàn thành 1 chuyến đi để nhận điểm thưởng',
    'DAILY',
    'COMPLETE_TRIPS',
    1,
    50,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    1,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 2. Daily Mission: Complete 3 trips
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Hoàn thành 3 chuyến đi',
    'Hoàn thành 3 chuyến đi trong ngày để nhận điểm thưởng',
    'DAILY',
    'COMPLETE_TRIPS',
    3,
    150,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    2,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 3. Weekly Mission: Complete 10 trips
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Hoàn thành 10 chuyến đi trong tuần',
    'Hoàn thành 10 chuyến đi trong tuần để nhận điểm thưởng lớn',
    'WEEKLY',
    'COMPLETE_TRIPS',
    10,
    500,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    3,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 4. Special Mission: Complete profile
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Hoàn thiện hồ sơ',
    'Cập nhật đầy đủ thông tin hồ sơ để nhận điểm thưởng',
    'SPECIAL',
    'COMPLETE_PROFILE',
    1,
    100,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    4,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 5. Special Mission: Verify driver license
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Xác thực bằng lái xe',
    'Xác thực bằng lái xe để trở thành tài xế và nhận điểm thưởng',
    'SPECIAL',
    'VERIFY_DRIVER_LICENSE',
    1,
    200,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    5,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 6. Daily Mission: Rate trips
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Đánh giá chuyến đi',
    'Đánh giá 5 chuyến đi để nhận điểm thưởng',
    'DAILY',
    'RATE_TRIPS',
    5,
    75,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    6,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 7. Weekly Mission: Invite friends
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Mời bạn bè',
    'Mời 3 bạn bè tham gia RideMate để nhận điểm thưởng',
    'WEEKLY',
    'INVITE_FRIENDS',
    3,
    300,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    7,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 8. Monthly Mission: Complete 50 trips
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Hoàn thành 50 chuyến đi trong tháng',
    'Hoàn thành 50 chuyến đi trong tháng để nhận điểm thưởng lớn',
    'MONTHLY',
    'COMPLETE_TRIPS',
    50,
    2000,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    8,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 9. Daily Mission: Consecutive login
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Đăng nhập liên tiếp 7 ngày',
    'Đăng nhập liên tiếp 7 ngày để nhận điểm thưởng',
    'DAILY',
    'CONSECUTIVE_DAYS',
    7,
    200,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    9,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 10. Event Mission: Use vouchers
INSERT INTO missions (
    title,
    description,
    mission_type,
    target_type,
    target_value,
    reward_points,
    start_date,
    end_date,
    is_active,
    priority,
    created_at,
    updated_at
) VALUES (
    'Sử dụng voucher',
    'Sử dụng 3 voucher để nhận điểm thưởng',
    'EVENT',
    'USE_VOUCHERS',
    3,
    150,
    NOW(),
    NOW() + INTERVAL '30 days',
    true,
    10,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

