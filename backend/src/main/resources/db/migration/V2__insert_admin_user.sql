-- Insert initial admin user
-- Default password: "admin123" (hashed with BCrypt)
-- IMPORTANT: Change this password after first login!
INSERT INTO users (id, email, password_hash, name, role, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin@example.com',
    '$2a$10$R8JmlnGD9G05IfJYZtZp.elgSgck/iKyc22kjJDyNYoQWbUqaeZNS', -- "admin123"
    'Admin User',
    'ADMIN',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;