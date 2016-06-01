#!/bin/sh
set -e

VERS=5.3.0
MC_VERS=1.9
MDK=1.9.4-12.17.0.1937
ARCHIVE=AutoSwitch-v$VERS-mc$MC_VERS.jar

CURRDIR="$PWD"

cd "$(dirname $0)"

mkdir -p build
cd build

if [ ! -e gradlew ] ; then
	cd ..
	TMP=AutoSwitch
	if [ -e AutoSwitch ] ; then
		TMP=$(mktemp)
		rm -f $TMP
		mv AutoSwitch $TMP
	fi
	mv build AutoSwitch
	cd AutoSwitch
	wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$MDK/forge-$MDK-mdk.zip
	unzip forge-$MDK-mdk.zip
	./gradlew setupDecompWorkspace
	./gradlew eclipse
	rm forge-$MDK-mdk.zip
	cd src/main
	rm -rf java resources
	ln -s ../../../resources
	ln -s ../../../src java
	cd ../..
	rm build.gradle
	ln -s ../build.gradle
	cd ..
	mv AutoSwitch build
	mv $TMP AutoSwitch 2>/dev/null || true
	cd build
fi

./gradlew build

cp build/libs/autoswitch-$VERS.jar $ARCHIVE
mkdir -p META-INF

cat >META-INF/MANIFEST.MF <<EOF
Manifest-Version: 1.0
Main-Class: thebombzen.mods.autoswitch.installer.ASInstallerFrame
EOF

zip -u $ARCHIVE META-INF/MANIFEST.MF
zip -d $ARCHIVE thebombzen/mods/thebombzenapi\*

cp $ARCHIVE "$CURRDIR"

