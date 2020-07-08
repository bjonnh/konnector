#!/usr/bin/env bash

set -euo pipefail

echo "Building..."
./batect build
echo

echo "Running unit tests..."
./batect test
echo

