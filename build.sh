#!/bin/sh
set -e

LONGNAME=AutoSwitch
LONGNAMELC=autoswitch
SHORTNAME=AS
VERS=5.3.1
MC_VERS=1.9.4
MDK=1.9.4-12.17.0.1987
ARCHIVE=$LONGNAME-v$VERS-mc$MC_VERS.jar

CURRDIR="$PWD"

cd "$(dirname $0)"

mkdir -p build
cd build

if [ ! -e gradlew ] ; then
	cd ..
	TMP=$LONGNAME
	if [ -e $LONGNAME ] ; then
		TMP=$(mktemp)
		rm -f $TMP
		mv $LONGNAME $TMP
	fi
	mv build $LONGNAME
	cd $LONGNAME
	wget http://files.minecraftforge.net/maven/net/minecraftforge/forge/$MDK/forge-$MDK-mdk.zip
	unzip forge-$MDK-mdk.zip
	./gradlew setupDecompWorkspace
	./gradlew eclipse
	rm forge-$MDK-mdk.zip
	cd src/main
	rm -rf java resources
	ln -s ../../../resources
	ln -s ../../../src java
	cd ../../..
	mv $LONGNAME build
	mv $TMP $LONGNAME 2>/dev/null || true
	cd build
fi

./gradlew build

cp build/libs/modid-1.0.jar $ARCHIVE
mkdir -p META-INF

echo "Manifest-Version: 1.0" >META-INF/MANIFEST.MF
echo "Main-Class: thebombzen.mods.${LONGNAMELC}.installer.${SHORTNAME}InstallerFrame" >>META-INF/MANIFEST.MF

zip -u $ARCHIVE META-INF/MANIFEST.MF
zip -d $ARCHIVE thebombzen/mods/thebombzenapi\*

cp $ARCHIVE "$CURRDIR"

