-- user table 설계
CREATE TABLE user_tb(
	id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(30) NOT NULL,
    fullname VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 사용자의 계좌 정보 table
CREATE TABLE account_tb(
	id INT AUTO_INCREMENT PRIMARY KEY,
    number VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(20) NOT NULL,
    balance BIGINT NOT NULL COMMENT '계좌 잔액',
	user_id INT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 사용자들의 history table 설계
-- 입금 내역 저장, 출금 내역 저장, 사용자 간 이체 내역 저장
-- BIGINT : 8바이트 크기의 정수형 데이터 타입
CREATE TABLE history_tb(
	id INT AUTO_INCREMENT PRIMARY KEY COMMENT '거래 내역 ID',
    amount BIGINT NOT NULL COMMENT '거래 금액',
    w_account_id INT COMMENT '출금 계좌 ID',
    d_account_id INT COMMENT '입금 계좌 ID',
    w_balance BIGINT COMMENT '출금 요청된 계좌 잔액',
    d_balance BIGINT COMMENT '입금 요청된 계좌 잔액',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);