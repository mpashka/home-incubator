CREATE USER qr_admin WITH PASSWORD 'qr_password';
CREATE DATABASE rs_receipt_qr OWNER your_user;
ALTER DATABASE rs_receipt_qr OWNER TO qr_admin;
GRANT ALL PRIVILEGES ON DATABASE rs_receipt_qr TO qr_admin;
GRANT ALL ON SCHEMA public TO qr_admin;
GRANT USAGE ON SCHEMA public TO qr_admin;
GRANT CREATE ON SCHEMA public TO qr_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO qr_admin;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO qr_admin;
