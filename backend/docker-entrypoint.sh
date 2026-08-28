#!/bin/sh

set -eu

if [ -z "${APP_JWT_SECRET:-}" ]; then
    if [ "${APP_JWT_GENERATE_IF_MISSING:-false}" != "true" ]; then
        echo "APP_JWT_SECRET must be configured." >&2
        exit 1
    fi

    APP_JWT_SECRET="$(od -An -N48 -tx1 /dev/urandom | tr -d ' \n')"
    export APP_JWT_SECRET

    echo "Using a temporary JWT secret generated for the local environment."
fi

exec java -jar /app/app.jar
