-- Seed data for Membership Packages
-- Run this script to populate initial membership packages

-- 1. RideMate Premium (MEM-1001)
INSERT INTO memberships (
    membership_id, 
    name, 
    description, 
    price, 
    duration, 
    max_trips_per_day, 
    point_multiplier, 
    benefits, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'MEM-1001',
    'RideMate Premium',
    'Ưu đãi đặc biệt mọi chuyến xe\nTích điểm nhanh gấp đôi',
    199000,
    30,
    5,
    2.0,
    '["Giảm 10% mọi chuyến đi", "Tích điểm x2", "Ưu tiên đặt chỗ", "Hỗ trợ 24/7"]'::jsonb,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 2. RideMate VIP (MEM-1002)
INSERT INTO memberships (
    membership_id, 
    name, 
    description, 
    price, 
    duration, 
    max_trips_per_day, 
    point_multiplier, 
    benefits, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'MEM-1002',
    'RideMate VIP',
    'Trải nghiệm dịch vụ cao cấp\nHỗ trợ ưu tiên 24/7',
    499000,
    30,
    10,
    3.0,
    '["Giảm 20% mọi chuyến đi", "Tích điểm x3", "Ưu tiên tối đa", "Hỗ trợ VIP 24/7", "Quà tặng đặc biệt"]'::jsonb,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 3. RideMate Family (MEM-1003)
INSERT INTO memberships (
    membership_id, 
    name, 
    description, 
    price, 
    duration, 
    max_trips_per_day, 
    point_multiplier, 
    benefits, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'MEM-1003',
    'RideMate Family',
    'Chia sẻ cho cả gia đình\nTối đa 5 thành viên',
    299000,
    30,
    8,
    2.5,
    '["Chia sẻ cho 5 người", "Giảm 15% mọi chuyến đi", "Tích điểm x2.5", "Ưu tiên đặt chỗ"]'::jsonb,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 4. RideMate Student (MEM-1004)
INSERT INTO memberships (
    membership_id, 
    name, 
    description, 
    price, 
    duration, 
    max_trips_per_day, 
    point_multiplier, 
    benefits, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'MEM-1004',
    'RideMate Student',
    'Gói đặc biệt cho sinh viên\nGiá ưu đãi',
    99000,
    30,
    3,
    1.5,
    '["Giảm 15% mọi chuyến đi", "Tích điểm x1.5", "Chỉ dành cho sinh viên"]'::jsonb,
    'PAUSED',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

-- 5. RideMate Basic (MEM-1005) - Gói cơ bản
INSERT INTO memberships (
    membership_id, 
    name, 
    description, 
    price, 
    duration, 
    max_trips_per_day, 
    point_multiplier, 
    benefits, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'MEM-1005',
    'RideMate Basic',
    'Gói cơ bản cho người mới bắt đầu\nTrải nghiệm membership với giá thấp',
    49000,
    30,
    2,
    1.2,
    '["Giảm 5% mọi chuyến đi", "Tích điểm x1.2", "Ưu tiên nhẹ"]'::jsonb,
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

