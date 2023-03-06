#!/usr/bin/env bash
set -ev

find "${HOME}/.m2/repository/" -type d -name "*-SNAPSHOT" | xargs -r -l rm -rf