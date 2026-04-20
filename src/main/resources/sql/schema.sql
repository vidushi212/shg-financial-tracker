-- Create Database
CREATE DATABASE IF NOT EXISTS shg_financial_db;
USE shg_financial_db;

-- SHG Groups Table
CREATE TABLE IF NOT EXISTS shg_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    location VARCHAR(255) NOT NULL,
    total_balance DECIMAL(19, 2) DEFAULT 0.00,
    monthly_contribution DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- SHG Members Table
CREATE TABLE IF NOT EXISTS shg_members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    role VARCHAR(50) NOT NULL,
    savings_amount DECIMAL(19, 2) DEFAULT 0.00,
    loan_amount DECIMAL(19, 2) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shg_group_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shg_group_id) REFERENCES shg_groups(id) ON DELETE CASCADE,
    INDEX idx_shg_group_id (shg_group_id),
    INDEX idx_username (username),
    INDEX idx_role (role)
);

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(500),
    recorded_by VARCHAR(255) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    shg_group_id BIGINT NOT NULL,
    member_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shg_group_id) REFERENCES shg_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES shg_members(id) ON DELETE SET NULL,
    INDEX idx_shg_group_id (shg_group_id),
    INDEX idx_type (type),
    INDEX idx_transaction_date (transaction_date)
);

-- Monthly Reports Table
CREATE TABLE IF NOT EXISTS monthly_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    month INT NOT NULL,
    year INT NOT NULL,
    total_savings DECIMAL(19, 2) DEFAULT 0.00,
    total_loans DECIMAL(19, 2) DEFAULT 0.00,
    total_expenses DECIMAL(19, 2) DEFAULT 0.00,
    total_balance DECIMAL(19, 2) DEFAULT 0.00,
    transaction_count INT DEFAULT 0,
    shg_group_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shg_group_id) REFERENCES shg_groups(id) ON DELETE CASCADE,
    INDEX idx_shg_group_id (shg_group_id),
    UNIQUE KEY unique_month_year (shg_group_id, month, year)
);

-- Investment Plans Table
CREATE TABLE IF NOT EXISTS investment_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    minimum_amount DECIMAL(19, 2) NOT NULL,
    expected_return DECIMAL(5, 2) NOT NULL,
    risk_level VARCHAR(50) NOT NULL,
    duration_months INT NOT NULL,
    broker_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_risk_level (risk_level),
    INDEX idx_broker_name (broker_name)
);

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'SBI Fixed Deposit', 'Guaranteed low-risk return for surplus SHG savings.', 1000.00, 8.50, 'Low', 12, 'State Bank of India', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'SBI Fixed Deposit');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Micro Enterprise Growth Plan', 'Structured investment plan for SHGs willing to take moderate risk.', 5000.00, 11.80, 'Medium', 24, 'RK Investments', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Micro Enterprise Growth Plan');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'HDFC Recurring Deposit Plus', 'Monthly contribution plan with stable returns for disciplined SHG savings.', 500.00, 7.90, 'Low', 18, 'HDFC Bank', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'HDFC Recurring Deposit Plus');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'ICICI Women Prosperity Deposit', 'Capital protection focused deposit tailored for women-led groups.', 2000.00, 8.10, 'Low', 15, 'ICICI Bank', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'ICICI Women Prosperity Deposit');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'NABARD Rural Growth Fund', 'Diversified rural development fund with moderate long-term appreciation.', 7500.00, 10.40, 'Medium', 36, 'NABARD', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'NABARD Rural Growth Fund');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Post Office Time Deposit', 'Government-backed fixed return option for secure reserve planning.', 1000.00, 7.70, 'Low', 24, 'India Post', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Post Office Time Deposit');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Axis Balanced Advantage Plan', 'Balanced allocation plan combining equity growth with debt stability.', 3000.00, 10.90, 'Medium', 30, 'Axis Mutual Fund', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Axis Balanced Advantage Plan');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'SBI Gold Savings Basket', 'Gold-linked savings product for inflation-aware long-term accumulation.', 2500.00, 9.20, 'Medium', 18, 'SBI Mutual Fund', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'SBI Gold Savings Basket');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Kotak Small Business Bond', 'Fixed-income bond product supporting predictable medium-term cash growth.', 4000.00, 9.80, 'Medium', 20, 'Kotak Securities', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Kotak Small Business Bond');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Mahila Udyam Equity Link', 'Higher-growth investment basket suited to SHGs building enterprise reserves.', 10000.00, 13.10, 'High', 36, 'Mahila Capital Advisors', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Mahila Udyam Equity Link');

INSERT INTO investment_plans (plan_name, description, minimum_amount, expected_return, risk_level, duration_months, broker_name, status)
SELECT 'Reliance Rural Infra Notes', 'Infrastructure-linked notes offering elevated return potential with higher risk.', 8000.00, 12.40, 'High', 30, 'Reliance Securities', 'APPROVED'
WHERE NOT EXISTS (SELECT 1 FROM investment_plans WHERE plan_name = 'Reliance Rural Infra Notes');

-- Government Schemes Table
CREATE TABLE IF NOT EXISTS government_schemes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scheme_name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    eligibility VARCHAR(1000),
    max_loan_amount DECIMAL(19, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    repayment_period_months INT NOT NULL,
    government_body VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_government_body (government_body)
);

-- Recommendations Table
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    recommendation_type VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    expected_benefit DECIMAL(19, 2) NOT NULL,
    generated_by VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_type (recommendation_type),
    INDEX idx_priority (priority)
);

-- Discussions Table
CREATE TABLE IF NOT EXISTS discussions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content VARCHAR(2000),
    created_by_username VARCHAR(255) NOT NULL,
    topic VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_topic (topic),
    INDEX idx_created_by (created_by_username)
);

-- Comments Table
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(1000) NOT NULL,
    author_username VARCHAR(255) NOT NULL,
    discussion_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (discussion_id) REFERENCES discussions(id) ON DELETE CASCADE,
    INDEX idx_discussion_id (discussion_id),
    INDEX idx_author_username (author_username)
);

-- Create Indexes for Better Performance
CREATE INDEX idx_shg_members_role_status ON shg_members(role, status);
CREATE INDEX idx_transactions_type_date ON transactions(type, transaction_date);
CREATE INDEX idx_recommendations_status_priority ON recommendations(status, priority);
