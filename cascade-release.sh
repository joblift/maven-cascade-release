#!/bin/bash -e

PROJECT_DIR=$1
OUTPUT_DIR=$2
RELATED_GROUP_IDS=$3
FILTERED_PROJECTS=$4

if [ ! -d "$PROJECT_DIR" ]; then
	echo "First argument has to be the parent-directory of the projects, a directory containing all the project directories with the pom.xml files"
	exit 1
fi
if [ ! -d "$OUTPUT_DIR" ]; then
	echo "Second argument must be a valid directory, where the rendered output files will be stored."
	exit 1
fi
if [ -z "$RELATED_GROUP_IDS" ]; then
	echo "At least one groupId has to be specified (multiple groupIds can be comma-separated - without spaces)"
fi
echo "cd src && groovy graph/GraphMaven.groovy $PROJECT_DIR $OUTPUT_DIR $RELATED_GROUP_IDS $FILTERED_PROJECTS "
( cd src && groovy cascade/MavenCascadeRelease.groovy $PROJECT_DIR $OUTPUT_DIR $RELATED_GROUP_IDS $FILTERED_PROJECTS )
DATE_FORMAT=`date +"%Y-%m-%d"`
dot -Tpng ${OUTPUT_DIR}/graph-${DATE_FORMAT}.dot -o ${OUTPUT_DIR}/dependencies-${DATE_FORMAT}.png

