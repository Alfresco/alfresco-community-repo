#!/usr/bin/env bash
#
# Restores the output of the "Prepare" job's single reactor build, so that a
# test job compiles nothing and only runs tests.
#
# IMPORTANT: this must run *after* init.sh. init.sh purges *-SNAPSHOT* from the
# local Maven repository, which would delete exactly the artifacts restored
# here.
#
# Counterpart: save_build_output.sh
#
echo "===================== Starting Restore Build Output ====================="
set -eo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
IN="${BUILD_OUTPUT_DIR:-/tmp/build-output}"

tar --zstd -xf "${IN}/reactor-targets.tzst" -C "${ROOT}"
tar --zstd -xf "${IN}/m2-alfresco.tzst" -C "${HOME}"

# actions/checkout stamps every source file with the time of the checkout,
# which is later than the mtimes preserved inside the tarball. Left alone,
# Maven's incremental check (and ajc's) would see every source as newer than
# its class file and recompile the whole reactor - precisely what restoring is
# meant to avoid. Re-stamp the restored output so that it is unambiguously
# newer than the sources it was built from.
find "${ROOT}" -path '*/target/*' -exec touch {} +

echo "Restored $(find "${ROOT}" -path '*/target/*' -type f | wc -l) build output files"
echo "==================== Finishing Restore Build Output ====================="
