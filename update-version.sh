#!/usr/bin/env bash

# This script is used for updating the version of the whole plugin.
#
# Usage: ./update-version.sh VERSION
# Example: ./update-version.sh 3.1.0-SNAPSHOT

# Note that automatically updating the version only works when the versions
# in the files have not been manually edited and actually match the current
# version.


VERSION="$*"

if [ -z "$VERSION" ]; then
	echo "update-version.sh VERSION"
	exit 1
fi

shopt -s globstar
OLD_VERSION=$(grep version gradle.properties | cut -d= -f2)
sed -ri "s/^version=$OLD_VERSION/version=$VERSION/g" **/gradle.properties
sed -ri "s/^Bundle-Version: $OLD_VERSION/Bundle-Version: $VERSION/g" **/MANIFEST.MF
sed -ri "s/$OLD_VERSION/$VERSION/g" **/feature.xml


