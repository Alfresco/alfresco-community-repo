#!/usr/bin/env bats

setup() {
    DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )" >/dev/null 2>&1 && pwd )"
    ACTION_SCRIPT="$DIR/../action.sh"

    export GITHUB_OUTPUT="$BATS_TMPDIR/test_downstream_msg_ghoutput_${RANDOM}.log"
    > "$GITHUB_OUTPUT"

    export COMMIT_TITLE="ACS-123: some regular change"
    export VERSION="1.2.3"
    export DOWNSTREAM_REPO="community-repo"
}

teardown() {
    rm -f "$GITHUB_OUTPUT"
}

# helper: read a named output from $GITHUB_OUTPUT
get_output() {
    grep "^$1=" "$GITHUB_OUTPUT" | cut -d= -f2-
}

@test "plain commit produces version-bump message" {
    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 1.2.3" ]
}

@test "plain commit sets allow-empty-commit=false" {
    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output allow-empty-commit)" = "false" ]
}

@test "[force] token is prepended to the message" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] Update community-repo version to 1.2.3" ]
}

@test "[force] token sets allow-empty-commit=true" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "[force ci] qualified token is preserved" {
    export COMMIT_TITLE="[force ci] ACS-456: force with qualifier"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force ci] Update community-repo version to 1.2.3" ]
}

@test "[force ci] qualified token sets allow-empty-commit=true" {
    export COMMIT_TITLE="[force ci] ACS-456: force with qualifier"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "downstream-repo name is embedded in message" {
    export DOWNSTREAM_REPO="enterprise-repo"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update enterprise-repo version to 1.2.3" ]
}

@test "version is embedded in message" {
    export VERSION="7.4.0"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 7.4.0" ]
}

@test "[force] token mid-title is extracted correctly" {
    export COMMIT_TITLE="ACS-789: some change [force] in the middle"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] Update community-repo version to 1.2.3" ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}
