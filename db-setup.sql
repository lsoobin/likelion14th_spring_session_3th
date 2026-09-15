-- 2주차 세션용 MySQL 실습 계정/DB 생성 스크립트
-- root 권한으로 1회 실행하세요.
CREATE DATABASE IF NOT EXISTS likelion_blog
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 아래 비밀번호 자리에는 로컬에서 DB_PASSWORD로 설정할 값을 입력하세요.
-- 실제 비밀번호로 바꾼 파일은 커밋하지 마세요.
CREATE USER IF NOT EXISTS 'likelion'@'localhost';
ALTER USER 'likelion'@'localhost' IDENTIFIED BY 'YOUR_DB_PASSWORD';
GRANT ALL PRIVILEGES ON likelion_blog.* TO 'likelion'@'localhost';
FLUSH PRIVILEGES;
