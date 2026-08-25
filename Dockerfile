# syntax=docker/dockerfile:1

# Google's Linux Android command-line tools target x86_64. Docker Desktop can
# emulate this isolated build environment on Apple Silicon.
ARG ANDROID_BUILD_PLATFORM=linux/amd64
FROM --platform=${ANDROID_BUILD_PLATFORM} eclipse-temurin:25.0.4_7-jdk-jammy@sha256:89565961a318534f01c971c7b1d030e60713c66995b887c94010cef938dbc53e

ARG ANDROID_COMMAND_LINE_TOOLS_VERSION=15859902
ARG ANDROID_COMMAND_LINE_TOOLS_SHA256=4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    PATH=/opt/android-sdk/cmdline-tools/latest/bin:${PATH}

RUN apt-get update \
    && apt-get install --yes --no-install-recommends ca-certificates curl unzip \
    && rm -rf /var/lib/apt/lists/* \
    && curl --fail --location --show-error \
        --output /tmp/android-command-line-tools.zip \
        "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_COMMAND_LINE_TOOLS_VERSION}_latest.zip" \
    && echo "${ANDROID_COMMAND_LINE_TOOLS_SHA256}  /tmp/android-command-line-tools.zip" | sha256sum --check - \
    && mkdir -p "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && unzip -q /tmp/android-command-line-tools.zip -d "${ANDROID_SDK_ROOT}/cmdline-tools" \
    && mv "${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools" "${ANDROID_SDK_ROOT}/cmdline-tools/latest" \
    && rm /tmp/android-command-line-tools.zip \
    && yes | sdkmanager --licenses >/dev/null \
    && sdkmanager --channel=0 \
        "build-tools;36.0.0" \
        "platforms;android-37.0"

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew

CMD ["./gradlew", ":app:testDebugUnitTest", ":app:assembleDebug", ":app:lintDebug", "--no-daemon"]
