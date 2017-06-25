#!/bin/bash -e

# get the absolute path of a directory
abs_dir() {
	echo "$(cd $1; pwd)"
}

# get the absolute path of the current script
script_dir() {
	echo $(abs_dir $(dirname $1))
}

SCRIPT_DIR=`script_dir $0`

BUILD_BINARY="$SCRIPT_DIR/target/libs/application.jar"
BUILD_REF_FILE="$SCRIPT_DIR/target/build.ref"
if [ -f "$BUILD_REF_FILE" ]; then
	BUILD_REF=`cat $BUILD_REF_FILE`
fi
LATEST_REF=`cd ${SCRIPT_DIR}; git rev-parse HEAD`

if [ "$#" -lt 1 ]; then
	echo "Usage: $0 <projects-dir> <start-project>"
	exit 1
fi



if [ "$1" == "rebuild" -o "$LATEST_REF" != "BUILD_REF" -o ! -f "BUILD_BINARY" ]; then
	(cd ${SCRIPT_DIR} && scripts/tests && scripts/package)
	if [ "$1" == "rebuild" ]; then
		shift
	fi
	echo "$LATEST_REF" > $BUILD_REF_FILE
fi
# Call application with compiled jar in the specific working directory
(cd ${SCRIPT_DIR} && java -jar ${BUILD_BINARY} ${*})
