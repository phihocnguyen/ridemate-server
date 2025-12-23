-- ============================================================================
-- PGVECTOR SETUP SCRIPT FOR RIDEMATE ID VERIFICATION
-- ============================================================================
-- 
-- Mục đích: Enable pgvector extension và setup database cho face verification
-- Chạy script này với: psql -U postgres -d ridemate_db -f setup_pgvector.sql
--
-- ============================================================================

-- Bước 1: Enable pgvector extension
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS vector;

-- Kiểm tra extension đã được cài đặt
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_extension WHERE extname = 'vector'
    ) THEN
        RAISE NOTICE '✓ pgvector extension đã được enable thành công';
        RAISE NOTICE 'Version: %', (SELECT extversion FROM pg_extension WHERE extname = 'vector');
    ELSE
        RAISE EXCEPTION '✗ Không thể enable pgvector extension. Vui lòng cài đặt pgvector trước.';
    END IF;
END $$;

-- Bước 2: Thêm các cột verification vào bảng users
-- ============================================================================

-- ID card image URL
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS id_card_image_url TEXT;

COMMENT ON COLUMN users.id_card_image_url IS 'URL của ảnh căn cước công dân được lưu trên Cloudinary';

-- ID card face embedding vector (512 dimensions) - từ ảnh CCCD
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS id_card_face_embedding vector(512);

COMMENT ON COLUMN users.id_card_face_embedding IS 'Face embedding 512 chiều từ ảnh CCCD (ID card), dùng để so khớp với selfie';

-- Selfie face embedding vector (512 dimensions) - từ ảnh selfie liveness check
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS face_embedding vector(512);

COMMENT ON COLUMN users.face_embedding IS 'Face embedding 512 chiều từ ảnh selfie (liveness check), dùng để so khớp với CCCD';

-- Verification status
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'UNVERIFIED';

COMMENT ON COLUMN users.verification_status IS 'Trạng thái xác thực: UNVERIFIED, PENDING, VERIFIED, REJECTED';

-- Verification date
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS verification_date TIMESTAMP;

COMMENT ON COLUMN users.verification_date IS 'Thời điểm hoàn tất xác thực';

-- Verification similarity score
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS verification_similarity_score FLOAT;

COMMENT ON COLUMN users.verification_similarity_score IS 'Điểm tương đồng giữa ảnh CCCD và selfie (0-1)';

-- Bước 3: Tạo index cho tìm kiếm similarity
-- ============================================================================

-- IVFFLAT index cho ID card face embedding
CREATE INDEX IF NOT EXISTS idx_users_id_card_face_embedding 
ON users USING ivfflat (id_card_face_embedding vector_cosine_ops)
WITH (lists = 100);

COMMENT ON INDEX idx_users_id_card_face_embedding IS 'IVFFLAT index cho tìm kiếm ID card face similarity';

-- IVFFLAT index cho selfie face embedding
CREATE INDEX IF NOT EXISTS idx_users_face_embedding 
ON users USING ivfflat (face_embedding vector_cosine_ops)
WITH (lists = 100);

COMMENT ON INDEX idx_users_face_embedding IS 'IVFFLAT index cho tìm kiếm selfie face similarity';

-- Lưu ý: Nếu dataset lớn hơn (> 100K users), có thể dùng HNSW index:
-- CREATE INDEX IF NOT EXISTS idx_users_face_embedding 
-- ON users USING hnsw (face_embedding vector_cosine_ops)
-- WITH (m = 16, ef_construction = 64);

-- Bước 4: Tạo constraint và validation
-- ============================================================================

-- Constraint: verification_status chỉ nhận các giá trị hợp lệ
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_verification_status'
    ) THEN
        ALTER TABLE users 
        ADD CONSTRAINT chk_verification_status 
        CHECK (verification_status IN ('UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED'));
    END IF;
END $$;

-- Constraint: similarity_score phải trong khoảng 0-1
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_similarity_score'
    ) THEN
        ALTER TABLE users 
        ADD CONSTRAINT chk_similarity_score 
        CHECK (verification_similarity_score IS NULL OR (verification_similarity_score >= 0 AND verification_similarity_score <= 1));
    END IF;
END $$;

-- Bước 5: Tạo helper functions
-- ============================================================================

-- Function: Tính cosine similarity giữa 2 embeddings
CREATE OR REPLACE FUNCTION calculate_face_similarity(
    embedding1 vector(512),
    embedding2 vector(512)
) RETURNS FLOAT AS $$
BEGIN
    -- Cosine similarity = 1 - cosine distance
    RETURN 1 - (embedding1 <=> embedding2);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

COMMENT ON FUNCTION calculate_face_similarity IS 'Tính cosine similarity giữa 2 face embeddings (0-1)';

-- Function: Tìm users có khuôn mặt giống nhất
CREATE OR REPLACE FUNCTION find_similar_faces(
    target_embedding vector(512),
    similarity_threshold FLOAT DEFAULT 0.7,
    max_results INT DEFAULT 10
) RETURNS TABLE (
    user_id BIGINT,
    full_name VARCHAR(100),
    similarity_score FLOAT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id,
        u.full_name,
        (1 - (u.face_embedding <=> target_embedding))::FLOAT as similarity
    FROM users u
    WHERE u.face_embedding IS NOT NULL
        AND (1 - (u.face_embedding <=> target_embedding)) >= similarity_threshold
    ORDER BY u.face_embedding <=> target_embedding
    LIMIT max_results;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION find_similar_faces IS 'Tìm users có khuôn mặt tương đồng với embedding cho trước';

-- Bước 6: Tạo view để theo dõi verification
-- ============================================================================

CREATE OR REPLACE VIEW verification_stats AS
SELECT 
    verification_status,
    COUNT(*) as total_users,
    AVG(verification_similarity_score) as avg_similarity_score,
    MIN(verification_date) as first_verification,
    MAX(verification_date) as last_verification
FROM users
WHERE verification_status IS NOT NULL
GROUP BY verification_status;

COMMENT ON VIEW verification_stats IS 'Thống kê trạng thái verification của users';

-- Bước 7: Set query optimization parameters
-- ============================================================================

-- Số partitions để search trong IVFFLAT index (default = 1)
-- Tăng lên để chính xác hơn nhưng chậm hơn
ALTER DATABASE ridemate_db SET ivfflat.probes = 10;

-- Bước 8: Verification và test
-- ============================================================================

-- Kiểm tra các cột đã được tạo
DO $$
DECLARE
    column_count INT;
BEGIN
    SELECT COUNT(*) INTO column_count
    FROM information_schema.columns
    WHERE table_name = 'users'
    AND column_name IN (
        'id_card_image_url',
        'id_card_face_embedding',
        'face_embedding',
        'verification_status',
        'verification_date',
        'verification_similarity_score'
    );
    
    IF column_count = 6 THEN
        RAISE NOTICE '✓ Tất cả 6 cột verification đã được tạo thành công';
    ELSE
        RAISE WARNING '⚠ Chỉ có % cột được tạo, cần kiểm tra lại', column_count;
    END IF;
END $$;

-- Kiểm tra index
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE indexname = 'idx_users_face_embedding'
    ) THEN
        RAISE NOTICE '✓ Index idx_users_face_embedding đã được tạo thành công';
    ELSE
        RAISE WARNING '⚠ Index chưa được tạo';
    END IF;
END $$;

-- Hiển thị thông tin bảng users
\echo ''
\echo '========================================='
\echo 'THÔNG TIN BẢNG USERS'
\echo '========================================='
\d users

-- Hiển thị các index
\echo ''
\echo '========================================='
\echo 'CÁC INDEX TRÊN BẢNG USERS'
\echo '========================================='
\di users*

-- Hiển thị verification stats
\echo ''
\echo '========================================='
\echo 'THỐNG KÊ VERIFICATION'
\echo '========================================='
SELECT * FROM verification_stats;

-- Kết thúc
\echo ''
\echo '========================================='
\echo '✓ HOÀN TẤT SETUP PGVECTOR'
\echo '========================================='
\echo 'Hệ thống đã sẵn sàng cho ID verification!'
\echo ''
\echo 'Các bước tiếp theo:'
\echo '1. Start Python face recognition service (port 5000)'
\echo '2. Start Spring Boot backend (port 8080)'
\echo '3. Test verification flow trên React Native app'
\echo ''
