#!/usr/bin/env bash
echo "=========================== Starting Release Script ==========================="
PS4="\[\e[35m\]+ \[\e[m\]"
set -vex
pushd "$(dirname "${BASH_SOURCE[0]}")/../../"

# Use full history for release
git checkout -B "${BRANCH_NAME}"

# Run the release plugin - with "[skip ci]" in the release commit message
mvn -B \
  -Pall-tas-tests \
  -Pags \
  "-Darguments=-Pall-tas-tests -Pags -DskipTests -Dbuild-number=${BUILD_NUMBER}" \
  release:clean release:prepare release:perform \
  -DscmCommentPrefix="[maven-release-plugin][skip ci] " \
  -Dusername="${GIT_USERNAME}" \
  -Dpassword="${GIT_PASSWORD}"

popd
set +vex
echo "=========================== Finishing Release Script =========================="

