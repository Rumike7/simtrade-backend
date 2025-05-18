#!/bin/bash

# Ports for each service
USER_SERVICE_PORT=8081
ORDER_SERVICE_PORT=8083
MARKET_SERVICE_PORT=8082

# PIDs of Spring Boot apps
declare -A spring_pids=(
    ["user_service"]=0
    ["order_service"]=0
    ["market_service"]=0
)

# Cleanup and exit (Ctrl+Z)
exit_script() {
    echo "Ctrl+Z detected. Stopping all services and exiting..."
    for service in "${!spring_pids[@]}"; do
        pid=${spring_pids[$service]}
        if [[ $pid -ne 0 ]]; then
            echo "Stopping $service (PID: $pid)..."
            kill -9 $pid 2>/dev/null
        fi
    done
    exit 0
}

# Restart all services (Ctrl+C)
restart_services() {
    echo "Ctrl+C detected. Restarting all services..."
    for service in "${!spring_pids[@]}"; do
        pid=${spring_pids[$service]}
        if [[ $pid -ne 0 ]]; then
            echo "Stopping $service (PID: $pid)..."
            kill -15 $pid 2>/dev/null  # SIGTERM for graceful shutdown
            sleep 1  # Wait for shutdown
            if kill -0 $pid 2>/dev/null; then  # Check if process still exists
                echo "Process $pid did not terminate, forcing kill..."
                kill -9 $pid 2>/dev/null
            fi
        fi
    done

    # Clean up any lingering processes on the ports
    declare -A ports=(
        ["user_service"]=$USER_SERVICE_PORT
        ["order_service"]=$ORDER_SERVICE_PORT
        ["market_service"]=$MARKET_SERVICE_PORT
    )

    for service in "${!ports[@]}"; do
        port=${ports[$service]}
        port_pid=$(lsof -ti :$port)
        if [[ -n $port_pid ]]; then
            echo "Killing processes on port $port for $service: $port_pid"
            kill -9 $port_pid 2>/dev/null
        fi
    done

    # Wait briefly before restarting
    sleep 1
}

# Trap Ctrl+Z and Ctrl+C
trap exit_script SIGTSTP
trap restart_services SIGINT

# Main loop
while true; do
    echo "Building the entire SimTrade project..."
    mvn clean install

    # Start each service in the background with its specific port
    echo "Starting user_service on port $USER_SERVICE_PORT..."
    cd user_service
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$USER_SERVICE_PORT" &
    spring_pids["user_service"]=$!
    cd ..

    echo "Starting order_service on port $ORDER_SERVICE_PORT..."
    cd order_service
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$ORDER_SERVICE_PORT" &
    spring_pids["order_service"]=$!
    cd ..

    echo "Starting market_service on port $MARKET_SERVICE_PORT..."
    cd market_service
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$MARKET_SERVICE_PORT" &
    spring_pids["market_service"]=$!
    cd ..

    # Wait for any of the services to stop
    for service in "${!spring_pids[@]}"; do
        pid=${spring_pids[$service]}
        wait $pid
        echo "$service has stopped."
    done

    echo "All services have stopped. Restarting..."
    sleep 1
done
