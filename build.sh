#!/bin/sh

VERS=5.2.2
MC_VERS=1.8

cd $(dirname $0)
ARCH=AutoSwitch-v$VERS-mc$MC_VERS.jar
./gradlew build || exit 1
cp build/libs/autoswitch-$VERS.jar $ARCH
mkdir -p META-INF

cat >META-INF/MANIFEST.MF <<EOF
Manifest-Version: 1.0
Main-Class: thebombzen.mods.autoswitch.installer.ASInstallerFrame
EOF

zip -u $ARCH META-INF/MANIFEST.MF
zip -d $ARCH "thebombzen/mods/thebombzenapi*"

