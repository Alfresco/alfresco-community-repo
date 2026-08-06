#!/usr/bin/env bats

setup() {
    DIR="$( cd "$( dirname "$BATS_TEST_FILENAME" )" >/dev/null 2>&1 && pwd )"
    ACTION_SCRIPT="$DIR/../action.sh"

    export GITHUB_OUTPUT="$BATS_TMPDIR/test_downstream_msg_ghoutput_${RANDOM}.log"
    > "$GITHUB_OUTPUT"

    export COMMIT_TITLE="ACS-123: some regular change"
    export VERSION="1.2.3"
    export DOWNSTREAM_REPO="community-repo"
    export PENDING_DOWNSTREAM=""
    export BRANCH_NAME=""
    export TRIGGER_RELEASE_ON_FORCE="false"
    export DIRECTIVES=""
}

teardown() {
    rm -f "$GITHUB_OUTPUT"
}

# helper: read a named output from $GITHUB_OUTPUT
# handles both key=value and heredoc (key<<EOF / value lines / EOF) formats
get_output() {
    local key="$1"
    # heredoc format: key<<EOF\n...\nEOF
    if grep -q "^${key}<<EOF" "$GITHUB_OUTPUT"; then
        awk "/^${key}<<EOF/{found=1; next} found && /^EOF/{exit} found{print}" "$GITHUB_OUTPUT"
    else
        grep "^${key}=" "$GITHUB_OUTPUT" | cut -d= -f2-
    fi
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

# downstream-repo omitted → bare version body

@test "downstream-repo omitted produces bare version as message body" {
    unset DOWNSTREAM_REPO
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "26.3.0-A.4" ]
}

@test "downstream-repo empty produces bare version as message body" {
    export DOWNSTREAM_REPO=""
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "26.3.0-A.4" ]
}

@test "downstream-repo omitted with directives=[release] produces '[release] <version>'" {
    unset DOWNSTREAM_REPO
    export DIRECTIVES="[release]"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[release] 26.3.0-A.4" ]
}

@test "downstream-repo omitted with directives=[release] and [publish] produces '[release][publish] <version>'" {
    unset DOWNSTREAM_REPO
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="ACS-123: publish this [publish]"
    export VERSION="26.2.0"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[release][publish] 26.2.0" ]
}

@test "downstream-repo omitted with [force] prefixes force token before version" {
    unset DOWNSTREAM_REPO
    export COMMIT_TITLE="[force] ACS-123: force release"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] 26.3.0-A.4" ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "downstream-repo omitted does not include repo name in message" {
    unset DOWNSTREAM_REPO
    export DIRECTIVES="[release]"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [[ "$(get_output message)" != *"version to"* ]]
}

# pending-downstream tests

@test "pending-downstream appends skip docker_release directive on non-master branch" {
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="Update community-repo version to 1.2.3

[skip docker_release] until alfresco-enterprise-share triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
}

@test "pending-downstream appends skip docker_latest directive on master branch" {
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="master"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="Update community-repo version to 1.2.3

[skip docker_latest] until alfresco-enterprise-share triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
}

@test "pending-downstream embeds the correct repo name in directive" {
    export PENDING_DOWNSTREAM="some-other-repo"
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [[ "$(get_output message)" == *"until some-other-repo triggers the build"* ]]
}

@test "pending-downstream does not affect allow-empty-commit" {
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output allow-empty-commit)" = "false" ]
}

@test "[force] token combined with pending-downstream prepends force and appends directive" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="[force] Update community-repo version to 1.2.3

[skip docker_release] until alfresco-enterprise-share triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "empty pending-downstream produces plain version-bump message" {
    export PENDING_DOWNSTREAM=""
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 1.2.3" ]
}

# trigger-release-on-force tests

@test "trigger-release-on-force=false with [force] does not add [release][skip tests]" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    export TRIGGER_RELEASE_ON_FORCE="false"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] Update community-repo version to 1.2.3" ]
}

@test "trigger-release-on-force omitted with [force] does not add [release][skip tests]" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    unset TRIGGER_RELEASE_ON_FORCE

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] Update community-repo version to 1.2.3" ]
}

@test "trigger-release-on-force=true with [force] appends [release][skip tests] after force token" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    export TRIGGER_RELEASE_ON_FORCE="true"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force][release][skip tests] Update community-repo version to 1.2.3" ]
}

@test "trigger-release-on-force=true with [force] still sets allow-empty-commit=true" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    export TRIGGER_RELEASE_ON_FORCE="true"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "trigger-release-on-force=true with versioned [force 26.3.0-A.7] appends [release][skip tests]" {
    export COMMIT_TITLE="ACS-123 bump [force 26.3.0-A.7]"
    export TRIGGER_RELEASE_ON_FORCE="true"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force 26.3.0-A.7][release][skip tests] Update community-repo version to 1.2.3" ]
}

@test "trigger-release-on-force=true without [force] produces plain message" {
    export COMMIT_TITLE="ACS-123: regular change"
    export TRIGGER_RELEASE_ON_FORCE="true"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 1.2.3" ]
    [ "$(get_output allow-empty-commit)" = "false" ]
}

@test "trigger-release-on-force=true combined with pending-downstream produces correct message" {
    export COMMIT_TITLE="[force] ACS-123: trigger downstream CI"
    export TRIGGER_RELEASE_ON_FORCE="true"
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="master"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="[force][release][skip tests] Update community-repo version to 1.2.3

[skip docker_latest] until alfresco-enterprise-share triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
}

# directives tests

@test "directives empty produces no directives prefix" {
    export DIRECTIVES=""

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 1.2.3" ]
}

@test "directives omitted produces no directives prefix" {
    unset DIRECTIVES

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "Update community-repo version to 1.2.3" ]
}

@test "directives=[release] without [publish] produces '[release] Update...' message" {
    export DIRECTIVES="[release]"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[release] Update community-repo version to 26.3.0-A.4" ]
}

@test "directives=[release] with [publish] in title produces '[release][publish] Update...' message" {
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="ACS-123: release [publish]"
    export VERSION="26.2.0"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[release][publish] Update community-repo version to 26.2.0" ]
}

@test "directives=[release] with [publish] at start of title is detected" {
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="[publish] ACS-123: release at start"
    export VERSION="26.2.0"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[release][publish] Update community-repo version to 26.2.0" ]
}

@test "directives without [publish] in title does not append [publish]" {
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="ACS-123: regular release, no publish"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [[ "$(get_output message)" != *"[publish]"* ]]
}

@test "directives=[release] combined with [force] prefixes force before directives" {
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="[force] ACS-123: force and release"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] [release] Update community-repo version to 26.3.0-A.4" ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "directives=[release] with [publish] combined with [force] produces correct message" {
    export DIRECTIVES="[release]"
    export COMMIT_TITLE="[force] ACS-123: force and publish [publish]"
    export VERSION="26.3.0-A.4"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    [ "$(get_output message)" = "[force] [release][publish] Update community-repo version to 26.3.0-A.4" ]
    [ "$(get_output allow-empty-commit)" = "true" ]
}

@test "directives=[release] combined with pending-downstream appends skip directive" {
    export DIRECTIVES="[release]"
    export VERSION="26.3.0-A.4"
    export PENDING_DOWNSTREAM="alfresco-enterprise-share"
    export BRANCH_NAME="feature/ACS-123"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="[release] Update community-repo version to 26.3.0-A.4

[skip docker_release] until alfresco-enterprise-share triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
}

@test "directives=[release] combined with downstream-repo omitted and pending-downstream appends skip directive" {
    unset DOWNSTREAM_REPO
    export DIRECTIVES="[release]"
    export VERSION="26.3.0-A.4"
    export PENDING_DOWNSTREAM="acs-community-packaging"
    export BRANCH_NAME="master"

    run bash "$ACTION_SCRIPT"

    [ "$status" -eq 0 ]
    expected="[release] 26.3.0-A.4

[skip docker_latest] until acs-community-packaging triggers the build or it is built manually"
    [ "$(get_output message)" = "$expected" ]
}
