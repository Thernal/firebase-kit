#!/bin/sh
# Sends a non-production build to Firebase App Distribution (firebase-kit) — the lanes in
# fastlane/FirebaseFastfile, from a shell or CI:
#
#   scripts/distribute-firebase.sh android <app> <flavor> [groups]
#   scripts/distribute-firebase.sh ios <flavor> [groups]
#
# Environment: see fastlane/FirebaseFastfile.
set -eu

usage() {
    sed -n '2,8p' "$0" | sed 's/^# \{0,1\}//' >&2
    exit 2
}

[ $# -ge 2 ] || usage
platform=$1
shift
cd "$(dirname "$0")/.."

case $platform in
    android)
        [ $# -ge 2 ] || usage
        set -- "app:$1" "flavor:$2" ${3:+"groups:$3"}
        ;;
    ios)
        set -- "flavor:$1" ${2:+"groups:$2"}
        ;;
    *) usage ;;
esac

exec bundle exec fastlane "$platform" firebase "$@"
