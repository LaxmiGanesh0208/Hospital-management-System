-- Ensure the 'users' table exists
CREATE TABLE IF NOT EXISTS "users" (
                                       id UUID PRIMARY KEY,
                                       email VARCHAR(255) UNIQUE NOT NULL,
                                       password VARCHAR(255) NOT NULL,
                                       role VARCHAR(50) NOT NULL,
                                       full_name VARCHAR(100),
                                       mobile VARCHAR(24),
                                       date_of_birth DATE,
                                       gender VARCHAR(30),
                                       address VARCHAR(255),
                                       emergency_contact VARCHAR(24)
);

ALTER TABLE "users" ADD COLUMN IF NOT EXISTS full_name VARCHAR(100);
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS mobile VARCHAR(24);
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS gender VARCHAR(30);
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(24);
CREATE UNIQUE INDEX IF NOT EXISTS users_mobile_unique_idx ON "users" (mobile);

CREATE TABLE IF NOT EXISTS revoked_tokens (
  token_hash VARCHAR(64) PRIMARY KEY,
  revoked_at TIMESTAMP NOT NULL
);

-- Insert the user if no existing user with the same id or email exists
INSERT INTO "users" (id, email, password, role)
SELECT '223e4567-e89b-12d3-a456-426614174006', 'testuser@test.com',
       '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu', 'ADMIN'
WHERE NOT EXISTS (
    SELECT 1
    FROM "users"
    WHERE id = '223e4567-e89b-12d3-a456-426614174006'
       OR email = 'testuser@test.com'
);

