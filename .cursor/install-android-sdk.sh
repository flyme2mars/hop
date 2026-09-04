#!/usr/bin/env bash
# Idempotent Cloud Agent bootstrap: JDK 17+ check + Android SDK cmdline tools.
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-${HOME}/Android/Sdk}"
export ANDROID_HOME
export ANDROID_SDK_ROOT="${ANDROID_HOME}"

CMDLINE_VERSION="15859902"
CMDLINE_ZIP="commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
CMDLINE_URL="https://dl.google.com/android/repository/${CMDLINE_ZIP}"
CMDLINE_SHA256="4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583"
SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"

need_cmd() {
  command -v "$1" >/dev/null 2>&1
}

if ! need_cmd unzip || ! need_cmd wget; then
  sudo apt-get update
  sudo apt-get install -y --no-install-recommends unzip wget
fi

# Gradle jvmToolchain(17) needs a real JDK 17, even if the default java is newer.
if [[ ! -x /usr/lib/jvm/java-17-openjdk-amd64/bin/java ]]; then
  sudo apt-get update
  sudo apt-get install -y --no-install-recommends openjdk-17-jdk-headless
fi

if ! need_cmd java; then
  sudo apt-get update
  sudo apt-get install -y --no-install-recommends openjdk-21-jdk-headless
fi

java_major="$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -n1)"
if [[ -z "${java_major}" || "${java_major}" -lt 17 ]]; then
  echo "JDK 17+ is required (found: $(java -version 2>&1 | head -n1))" >&2
  exit 1
fi

if [[ ! -x "${SDKMANAGER}" ]]; then
  tmp="$(mktemp -d)"
  trap 'rm -rf "${tmp}"' EXIT
  wget -q -O "${tmp}/${CMDLINE_ZIP}" "${CMDLINE_URL}"
  echo "${CMDLINE_SHA256}  ${tmp}/${CMDLINE_ZIP}" | sha256sum -c -
  unzip -q "${tmp}/${CMDLINE_ZIP}" -d "${tmp}"
  mkdir -p "${ANDROID_HOME}/cmdline-tools"
  rm -rf "${ANDROID_HOME}/cmdline-tools/latest"
  mv "${tmp}/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest"
fi

yes | "${SDKMANAGER}" --sdk_root="${ANDROID_HOME}" --licenses >/dev/null || true
"${SDKMANAGER}" --sdk_root="${ANDROID_HOME}" --install \
  "platform-tools" \
  "platforms;android-36" \
  "platforms;android-37.0" \
  "build-tools;36.0.0"

profile="${HOME}/.bashrc"
marker="# hop-android-sdk"
if [[ -f "${profile}" ]] && grep -q "${marker}" "${profile}"; then
  :
else
  {
    echo ""
    echo "${marker}"
    echo "export ANDROID_HOME=\"${ANDROID_HOME}\""
    echo "export ANDROID_SDK_ROOT=\"\${ANDROID_HOME}\""
    echo "export PATH=\"\${ANDROID_HOME}/cmdline-tools/latest/bin:\${ANDROID_HOME}/platform-tools:\${PATH}\""
  } >> "${profile}"
fi

echo "Android SDK ready at ${ANDROID_HOME}"
