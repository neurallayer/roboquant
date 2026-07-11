#!/bin/bash
# Convert Maven-style source directories to Kotlin Toolchain layout.
# Assumes module.yaml exists in each module.

set -e

# Find all directories containing module.yaml (relative to given root, default .)
find_modules() {
    find "$1" -name "module.yaml" -exec dirname {} \;
}

root="${1:-.}"
for module in $(find_modules "$root"); do
    echo "Processing: $module"
    ( cd "$module"

        # Main sources
        if [ -d src/main/java ] && [ -n "$(ls -A src/main/java 2>/dev/null)" ]; then
            mkdir -p src
            mv src/main/java/* src/
        fi
        if [ -d src/main/kotlin ] && [ -n "$(ls -A src/main/kotlin 2>/dev/null)" ]; then
            mkdir -p src
            mv src/main/kotlin/* src/
        fi

        # Main resources
        if [ -d src/main/resources ] && [ -n "$(ls -A src/main/resources 2>/dev/null)" ]; then
            mkdir -p resources
            mv src/main/resources/* resources/
        fi

        # Test sources
        if [ -d src/test/java ] && [ -n "$(ls -A src/test/java 2>/dev/null)" ]; then
            mkdir -p test
            mv src/test/java/* test/
        fi
        if [ -d src/test/kotlin ] && [ -n "$(ls -A src/test/kotlin 2>/dev/null)" ]; then
            mkdir -p test
            mv src/test/kotlin/* test/
        fi

        # Test resources
        if [ -d src/test/resources ] && [ -n "$(ls -A src/test/resources 2>/dev/null)" ]; then
            mkdir -p testResources
            mv src/test/resources/* testResources/
        fi

        # Remove now‑empty Maven directories (ignore failures)
        rmdir src/main/java src/main/kotlin src/main/resources 2>/dev/null || true
        rmdir src/test/java src/test/kotlin src/test/resources 2>/dev/null || true
        rmdir src/main src/test 2>/dev/null || true
    )
done

echo "All modules processed."