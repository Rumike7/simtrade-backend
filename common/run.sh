#!/bin/bash

# PID of Spring Boot app
spring_pid=0

# Cleanup and exit (Ctrl+Z)
exit_script() {
    echo "Ctrl+Z detected. Stopping Spring Boot and exiting..."
    if [[ $spring_pid -ne 0 ]]; then
        kill -9 $spring_pid 2>/dev/null
    fi
    exit 0
}

# Restart Spring Boot (Ctrl+C)
restart_spring() {
    echo "Ctrl+C detected. Restarting Spring Boot..."
    if [[ $spring_pid -ne 0 ]]; then
        kill -15 $spring_pid 2>/dev/null  # SIGTERM for graceful shutdown
        sleep 1  # Wait for shutdown
        if kill -0 $spring_pid 2>/dev/null; then  # Check if process still exists
            echo "Process $spring_pid did not terminate, forcing kill..."
            kill -9 $spring_pid 2>/dev/null
        fi
    fi
    # Clean up any lingering processes on port 8082
    port_pid=$(lsof -ti :8082)
    if [[ -n $port_pid ]]; then
        echo "Killing processes on port 8082: $port_pid"
        kill -9 $port_pid 2>/dev/null
    fi
    # Wait briefly before restarting
    sleep 1
}

# Trap Ctrl+Z and Ctrl+C
trap exit_script SIGTSTP
trap restart_spring SIGINT

while true; do
    mvn clean package

    mvn spring-boot:run &
    spring_pid=$!
    wait $spring_pid

    echo "Spring Boot has stopped. Restarting..."
    sleep 1
done
