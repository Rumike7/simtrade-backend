FROM timescale/timescaledb:latest-pg16

# Copy the initialization script
COPY ./sql/init-db.sql /docker-entrypoint-initdb.d/init-db.sql