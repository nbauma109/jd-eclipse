#!/usr/bin/env bash
export VERSION=$(grep version gradle.properties | cut -d= -f2)
mvn -B deploy:deploy-file -Dfile=build/distributions/jd-eclipse-${VERSION}.zip -DgroupId=com.github.nbauma109 -DartifactId=jd-eclipse -Dversion=${VERSION} -Dpackaging=zip -DrepositoryId=jitci -Durl=file:///home/jitpack/deploy