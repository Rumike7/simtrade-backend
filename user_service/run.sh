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
        kill -9 $spring_pid 2>/dev/null
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
