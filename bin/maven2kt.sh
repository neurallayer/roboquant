#!/bin/bash
# Convert Maven-style source directories to Kotlin Toolchain layout.
# Assumes module.yaml exists in each module.

set -e

# Find all directories containing module.yaml (relative to given root, default .)
find_modules() {
    find "$1" -name "module.yaml" -exec dirname {} \;
}

# Merge src into dest: merge contents, then remove src.
mv_merge() {
    local src="$1"
    local dest="$2"
    if [ -d "$src" ] && [ -n "$(ls -A "$src" 2>/dev/null)" ]; then
        mkdir -p "$dest"
        cp -r "$src/." "$dest/"
        rm -rf "$src"
    fi
}

root="${1:-.}"
for module in $(find_modules "$root"); do
    echo "Processing: $module"
    ( cd "$module"

      # main
      mv_merge "src/main/java"   "src"
      mv_merge "src/main/kotlin" "src"
      mv_merge "src/main/resources" "resources"

      # Test
      mv_merge "src/test/java"   "test"
      mv_merge "src/test/kotlin" "test"
      mv_merge "src/test/resources" "testResources"

    )
done

echo "All modules processed."