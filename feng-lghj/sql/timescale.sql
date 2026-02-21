-- Initialize TimescaleDB tables

-- Daily K-Line
DROP table if exists stock_kline_daily;
CREATE TABLE IF NOT EXISTS stock_kline_daily
(
    code       VARCHAR(20) NOT NULL,
    trade_date TIMESTAMP   NOT NULL,
    open       DECIMAL(20, 4),
    close      DECIMAL(20, 4),
    high       DECIMAL(20, 4),
    low        DECIMAL(20, 4),
    volume     BIGINT,
    amount     DECIMAL(20, 4),
    PRIMARY KEY (code, trade_date)
);

-- Monthly K-Line
DROP table if exists stock_kline_monthly;
CREATE TABLE IF NOT EXISTS stock_kline_monthly
(
    code       VARCHAR(20) NOT NULL,
    trade_date TIMESTAMP   NOT NULL,
    open       DECIMAL(20, 4),
    close      DECIMAL(20, 4),
    high       DECIMAL(20, 4),
    low        DECIMAL(20, 4),
    volume     BIGINT,
    amount     DECIMAL(20, 4),
    PRIMARY KEY (code, trade_date)
);

-- Weekly K-Line
DROP table if exists stock_kline_weekly;
CREATE TABLE IF NOT EXISTS stock_kline_weekly
(
    code       VARCHAR(20) NOT NULL,
    trade_date TIMESTAMP   NOT NULL,
    open       DECIMAL(20, 4),
    close      DECIMAL(20, 4),
    high       DECIMAL(20, 4),
    low        DECIMAL(20, 4),
    volume     BIGINT,
    amount     DECIMAL(20, 4),
    PRIMARY KEY (code, trade_date)
);

-- Convert to hypertables
SELECT create_hypertable('stock_kline_daily', 'trade_date', if_not_exists => TRUE);

SELECT create_hypertable('stock_kline_monthly', 'trade_date', if_not_exists => TRUE);

SELECT create_hypertable('stock_kline_weekly', 'trade_date', if_not_exists => TRUE);