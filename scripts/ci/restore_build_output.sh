#!/usr/bin/env bash
#
# Restores the reactor build output packed by save_build_output.sh, so that a test job runs its tests without
# rebuilding the reactor.
#
# Must run after init.sh, which deletes every *-SNAPSHOT artifact from ~/.m2 - including the ones restored here.
# Counterpart: save_build_output.sh
#
echo "========================== Starting Restore Build Output ========================="
set -eo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
IN="${BUILD_OUTPUT_DIR:-/tmp/build-output}"

tar -I zstd -xf "${IN}/reactor-targets.tar.zst" -C "${ROOT}"
tar -I zstd -xf "${IN}/m2-snapshots.tar.zst" -C "${HOME}"

# actions/checkout stamps every source file with the checkout time, which is later than the mtimes preserved in
# the archive, so Maven would consider all classes stale and recompile the reactor. Give all restored output one
# common timestamp: newer than any source, and never newer than each other (avoids "dependency changed" rebuilds).
STAMP="$(date +%s)"
find "${ROOT}" -path '*/target/*' -not -path "${ROOT}/.git/*" -exec touch -d "@${STAMP}" {} +

echo "Restored $(find "${ROOT}" -path '*/target/*' -type f | wc -l) build output files"
echo "========================== Finishing Restore Build Output ========================"
