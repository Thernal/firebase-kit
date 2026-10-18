#!/usr/bin/env bash
# Sends a non-production build to Firebase App Distribution (firebase-kit).
#
#   scripts/distribute-firebase.sh android <app> <flavor> [debug|release] [options]
#   scripts/distribute-firebase.sh ios <flavor> [groups]
#
# Android — the Firebase CLI, no Ruby needed. Build type defaults to release. Options:
#   --groups a,b     tester groups (aliases from the console); default $FIREBASE_GROUPS
#   --testers x@y,…  single testers (emails)
#   --notes "text"   release notes; default "<flavor> <build type> · <short sha> (<branch>)"
#   --no-bump        keep the versionCode: by default a build that is sent first raises the flavor's
#                    versionCode by one (scripts/bump-version-code.sh, in the working tree; nothing is committed).
#                    --no-build and --dry-run never bump: the APK sent is the one built before.
#   --no-build       send the APK of the last build instead of building one
#   --dry-run        build and check, say what would be sent, send nothing
# With neither groups nor testers the build is uploaded and distributed to nobody.
#
# From make:  make distribute-android APP=customer FLAVOR=beta BUILD_TYPE=release TESTER_GROUPS=qa
#
# Signing: a non-production variant is signed with the team's shared debug keystore, else AGP's debug key
# (build-logic/…/Signing.kt), so the APK installs; this script refuses an unsigned one.
#
# Firebase app id: FIREBASE_APP_ID_ANDROID_<APP>_<FLAVOR> if set, else read from the flavor's
# google-services.json by the build's applicationId. Credentials: GOOGLE_APPLICATION_CREDENTIALS (a service
# account with the App Distribution Admin role; relative paths from the repository root), else the Firebase
# CLI's own `firebase login`. Both and the groups can sit in fastlane/.env.firebase (git-ignored).
#
# Needs build-kit's scripts/app-properties.sh and bump-version-code.sh and its `make build-android`. The third
# argument may also be a group alias (`android customer beta qa`), as before. iOS goes through fastlane
# (fastlane/FirebaseFastfile): see the firebase-kit skill, which also has the fastlane lane for Android.
set -euo pipefail

source "$(dirname "${BASH_SOURCE[0]}")/app-properties.sh"
cd "$REPO_ROOT"

usage() {
  sed -n '2,30p' "$0" | sed 's/^# \{0,1\}//' >&2
  exit 2
}

[ $# -ge 2 ] || usage
platform=$1
shift

if [ "$platform" = "ios" ]; then
  flavor=$1
  shift
  exec bundle exec fastlane ios firebase "flavor:$flavor" ${1:+"groups:$1"}
fi
[ "$platform" = "android" ] || usage

app=${1:-}
flavor=${2:-}
shift 2 || usage
require_app "$app"
require_flavor "$flavor"
if [ "$flavor" = "$PRODUCTION_FLAVOR" ]; then
  echo "$flavor is the production flavor: it goes to the stores, not to testers" >&2
  exit 2
fi

build_type=release
groups=""
testers=""
notes=""
bump=auto
build=true
dry_run=false
while [ $# -gt 0 ]; do
  case $1 in
    debug|release) build_type=$1 ;;
    --groups) groups=${2:?--groups needs a value}; shift ;;
    --testers) testers=${2:?--testers needs a value}; shift ;;
    --notes) notes=${2:?--notes needs a value}; shift ;;
    --bump) bump=true ;;
    --no-bump) bump=false ;;
    --no-build) build=false ;;
    --dry-run) dry_run=true ;;
    -*) usage ;;
    *) [ -z "$groups" ] && groups=$1 || usage ;;
  esac
  shift
done

env_file="$REPO_ROOT/fastlane/.env.firebase"
if [ -f "$env_file" ]; then
  set -a
  # shellcheck disable=SC1090
  source "$env_file"
  set +a
fi
groups=${groups:-${FIREBASE_GROUPS:-}}

# Every release testers get carries its own versionCode, so a phone tells the builds apart and installs the
# newer one over the older.
if [ "$bump" = auto ]; then
  if $build && ! $dry_run; then bump=true; else bump=false; fi
fi
if $bump && ! $build; then
  echo "--bump with --no-build would send an APK that does not carry the new versionCode" >&2
  exit 2
fi
if $bump; then
  scripts/bump-version-code.sh "$app" "$flavor"
fi
if $build; then
  make --no-print-directory build-android APP="$app" FLAVOR="$flavor" BUILD_TYPE="$build_type"
fi

apk_dir="$APPS_DIR/$app/android/build/outputs/apk/$flavor/$build_type"
apk=$(ls -t "$apk_dir"/*.apk 2>/dev/null | head -n1 || true)
if [ -z "$apk" ]; then
  echo "No APK under $apk_dir — run without --no-build" >&2
  exit 1
fi

# The APK has to install: a signature is required, whichever key made it.
sdk=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}
apksigner=$(ls -d "$sdk"/build-tools/*/apksigner 2>/dev/null | sort -V | tail -n1 || true)
if [ -n "$apksigner" ]; then
  certs=$("$apksigner" verify --print-certs "$apk" 2>&1) || {
    echo "$apk is not signed with a valid signature:" >&2
    echo "$certs" >&2
    exit 1
  }
  signer=$(printf '%s\n' "$certs" | sed -n 's/^Signer #1 certificate DN: //p' | head -n1)
else
  signer="(apksigner not found under $sdk/build-tools; signature not checked)"
fi

app_id_var="FIREBASE_APP_ID_ANDROID_$(printf '%s_%s' "$app" "$flavor" | tr '[:lower:]-' '[:upper:]_')"
app_id=${!app_id_var:-}
if [ -z "$app_id" ]; then
  app_id=$(python3 - "$APPS_DIR/$app/android/src/$flavor/google-services.json" "$apk_dir/output-metadata.json" <<'PY'
import json, sys
services, metadata = sys.argv[1:3]
application_id = json.load(open(metadata))["applicationId"]
for client in json.load(open(services))["client"]:
    info = client["client_info"]
    if info["android_client_info"]["package_name"] == application_id:
        print(info["mobilesdk_app_id"])
        break
PY
  ) || true
fi
if [ -z "$app_id" ]; then
  echo "No Firebase app id for $app $flavor: set $app_id_var, or add the build's applicationId to its google-services.json" >&2
  exit 1
fi

if [ -z "$notes" ]; then
  notes="$flavor $build_type · $(git rev-parse --short HEAD) ($(git branch --show-current))"
fi
credentials=${GOOGLE_APPLICATION_CREDENTIALS:-}
if [ -n "$credentials" ] && [ ! -f "$credentials" ] && [ -f "$REPO_ROOT/$credentials" ]; then
  credentials="$REPO_ROOT/$credentials"
fi

version=$(python3 - "$apk_dir/output-metadata.json" <<'PY' || true
import json, sys
element = json.load(open(sys.argv[1]))["elements"][0]
print(f"{element.get('versionName', '?')} ({element.get('versionCode', '?')})")
PY
)

cat <<EOF
app         $app · $flavor · $build_type
version     ${version:-unknown}
apk         $apk ($(du -h "$apk" | cut -f1))
signed by   $signer
firebase    $app_id
groups      ${groups:-—}
testers     ${testers:-—}
notes       $notes
credentials ${credentials:-the Firebase CLI login}
EOF
if $dry_run; then
  echo "dry run: nothing was sent"
  exit 0
fi

if command -v firebase >/dev/null 2>&1; then
  cli=(firebase)
else
  cli=(npx --yes firebase-tools@latest)
fi
args=(appdistribution:distribute "$apk" --app "$app_id" --release-notes "$notes")
[ -z "$groups" ] || args+=(--groups "$groups")
[ -z "$testers" ] || args+=(--testers "$testers")
if [ -n "$credentials" ]; then
  GOOGLE_APPLICATION_CREDENTIALS=$credentials "${cli[@]}" "${args[@]}"
else
  "${cli[@]}" "${args[@]}"
fi
