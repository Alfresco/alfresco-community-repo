#!/usr/bin/env bash
#
# Packs the output of the single reactor build done by the "Prepare" job, so that downstream test jobs can
# restore it instead of rebuilding the reactor from source.
#
# Produces two archives under ${BUILD_OUTPUT_DIR}:
#   reactor-targets.tar.zst  every module's target/ directory (classes, test classes, jars, wars, amps)
#   m2-snapshots.tar.zst     the org/alfresco *-SNAPSHOT artifacts this build installed into ~/.m2
#
# Must run before cleanup_cache.sh, which deletes the SNAPSHOT artifacts from ~/.m2.
# Counterpart: restore_build_output.sh
#
echo "=========================== Starting Save Build Output ==========================="
set -eo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
OUT="${BUILD_OUTPUT_DIR:-/tmp/build-output}"
M2_ALFRESCO=".m2/repository/org/alfresco"

mkdir -p "${OUT}"
cd "${ROOT}"

# Top-level target/ directory of every module; nested ones (e.g. exploded wars) are inside them already.
find . -type d -name target -not -path '*/target/*' -not -path './.git/*' | sort > "${OUT}/target-dirs.txt"
echo "Packing $(wc -l < "${OUT}/target-dirs.txt") target directories"

# Test reports, logs and content stores are per-job output; leaking them from this job would only add noise.
tar -I "zstd -3 -T0" -cf "${OUT}/reactor-targets.tar.zst" \
    --exclude='surefire-reports' \
    --exclude='failsafe-reports' \
    --exclude='alf_data*' \
    --exclude='*.log' \
    -T "${OUT}/target-dirs.txt"

(cd "${HOME}" && find "${M2_ALFRESCO}" -type d -name '*-SNAPSHOT' -prune | sort) > "${OUT}/snapshot-dirs.txt"
echo "Packing $(wc -l < "${OUT}/snapshot-dirs.txt") SNAPSHOT artifact directories from ~/${M2_ALFRESCO}"
tar -I "zstd -3 -T0" -cf "${OUT}/m2-snapshots.tar.zst" -C "${HOME}" -T "${OUT}/snapshot-dirs.txt"

rm -f "${OUT}/target-dirs.txt" "${OUT}/snapshot-dirs.txt"
ls -lh "${OUT}"
echo "=========================== Finishing Save Build Output =========================="
