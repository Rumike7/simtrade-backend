DO $$ 
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'simtrade_db') THEN
      PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE simtrade_db');
   END IF;
END $$;

-- Connect to the new database
\connect simtrade_db

-- Enable TimescaleDB extension
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Example: Convert 'users' to a hypertable (if using TimescaleDB features)
-- SELECT create_hypertable('users', 'created_at', if_not_exists => TRUE);