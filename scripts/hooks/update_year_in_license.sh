#!/bin/bash
set -euo pipefail

CURRENT_YEAR=$(date +%Y)

# Collect staged .java files that are newly added (A) or modified (M)
FILES=$( (git diff --name-only --diff-filter=AM; git diff --cached --name-only --diff-filter=AM) | grep -E '\.java$' | sort -u )

for file in $FILES; do
  # Skip if file doesn't exist (e.g., deleted in another branch)
  [ -f "$file" ] || continue

  # Check if the file contains a copyright line
  if grep -qE "Copyright \(C\) [0-9]{4}(\s*-\s*[0-9]{4})? Alfresco Software Limited\.?" "$file"; then
    # Replace the year or year range with updated range
    sed -i.bak -E \
      "s/(Copyright \(C\) )([0-9]{4})(\s*-\s*[0-9]{4})?( Alfresco Software Limited\.?)/\1\2 - $CURRENT_YEAR\4/g" \
      "$file"

    # Only stage the file if changes were actually made
    if ! cmp -s "$file" "${file}.bak"; then
      rm "${file}.bak"
      git add "$file"
      echo "✅ Updated $file"
    else
      rm "${file}.bak"
    fi
  fi
done

exit 0
