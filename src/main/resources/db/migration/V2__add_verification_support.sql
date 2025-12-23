-- Enable pgvector extension for face embedding similarity search
CREATE EXTENSION IF NOT EXISTS vector;

-- Add new columns to users table for ID verification
-- Note: If columns already exist, these statements will fail. Run them individually if needed.

-- ID card image URL
ALTER TABLE users ADD COLUMN IF NOT EXISTS id_card_image_url TEXT;

-- Face embedding as 512-dimensional vector
ALTER TABLE users ADD COLUMN IF NOT EXISTS face_embedding vector(512);

-- Verification status
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_status VARCHAR(20) DEFAULT 'UNVERIFIED';

-- Verification date
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_date TIMESTAMP;

-- Verification similarity score
ALTER TABLE users ADD COLUMN IF NOT EXISTS verification_similarity_score FLOAT;

-- Create index on face_embedding for efficient similarity search using cosine distance
CREATE INDEX IF NOT EXISTS idx_users_face_embedding ON users USING ivfflat (face_embedding vector_cosine_ops);

-- Add comment to explain the verification status values
COMMENT ON COLUMN users.verification_status IS 'Verification status: UNVERIFIED, PENDING, VERIFIED, REJECTED';
