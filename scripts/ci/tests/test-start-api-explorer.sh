#!/usr/bin/env bash

# Function to perform cURL request with timeout
function perform_curl() {
    local url=$1
    local max_retries=$2
    local retry_delay=$3
    local timeout=$4

    local retry=0
    local exit_code=0

    while [ $retry -lt $max_retries ]; do
        curl --max-time $timeout $url

        exit_code=$?
        if [ $exit_code -eq 0 ]; then
            echo "Webpage is available"
            return 0
        else
            echo "Webpage not available (retry: $((retry+1)))"
            sleep $retry_delay
            retry=$((retry+1))
        fi
    done

    echo "Maximum number of retries reached"
    return $exit_code
}

echo "=========================== Starting api-explorer deployment test ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vx
pushd "$(dirname "${BASH_SOURCE[0]}")/../../../"

# Execute Maven command in the background
mvn clean install -Pags,start-api-explorer -DskipTests &
# Get the PID of the Maven command
maven_pid=$!

# Wait for the Maven command to start the Tomcat server
sleep 120

# Call the function with desired parameters
perform_curl "http://localhost:8085/api-explorer" 5 10 5
exit_code=$?

# Stop the Tomcat server by sending termination signal to the Maven process
kill -SIGTERM $maven_pid &

popd
set +vx
echo "=========================== Ending api-explorer deployment test =========================="

exit $exit_code