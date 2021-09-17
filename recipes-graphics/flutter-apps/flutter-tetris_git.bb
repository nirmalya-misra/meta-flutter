LICENSE = "CLOSED"
#LICENSE = "MIT"
#LIC_FILES_CHKSUM = "file://LICENSE-MIT;md5=09b77fb74986791a3d4a0e746a37d88f"

#LIC_FILES_CHKSUM = "file://LICENSE;md5=7e9e54c31cefc5b0e5186e7ada75ea2ece4496ab"
#LICENSE = "BSD-3-Clause"
#LIC_FILES_CHKSUM = "file://LICENSE;md5=1e9373ff121f393d20bac6493d67411f"

DEPENDS += "\
    flutter-engine \
    flutter-sdk-native \
    unzip-native \
    libxkbcommon \
   "
RDEPENDS_${PN} += "xkeyboard-config"

SRC_URI = "git://github.com/boyan01/flutter-tetris.git;lfs=0;protocol=https;destsuffix=git"
SRC_URI[sha256sum] = "5ea2f94db777419048d7a3523a577f24938bce2278932999064b48af0ebffd83"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

do_patch[depends] += "flutter-sdk-native:do_populate_sysroot"

do_patch() {
    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH
    export PUB_CACHE=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/.pub-cache

    FLUTTER_VER="$( flutter --version | head -n 1 | awk '{print $2}' )"
    echo "Flutter Version: ${FLUTTER_VER}"
}

do_configure() {
    #
    # Engine SDK
    #
    rm -rf ${S}/engine_sdk
    unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH

    ENGINE_SDK=${S}/engine_sdk/sdk

    cd ${S}
    flutter --version
    flutter build bundle
    dart ${ENGINE_SDK}/frontend_server.dart.snapshot --aot --tfa --target=flutter --sdk-root ${ENGINE_SDK} --output-dill app.dill lib/main.dart
    ${ENGINE_SDK}/clang_x64/gen_snapshot --deterministic --snapshot_kind=app-aot-elf --elf=libapp.so --strip app.dill
}

do_install() {
    install -d ${D}${datadir}/${PN}/tetris/flutter_assets/
    cp -rTv ${S}/build/flutter_assets/. ${D}${datadir}/${PN}/tetris/flutter_assets/
}

FILES_${PN} = "${datadir}/${PN}/*"

do_package_qa[noexec] = "1"

