#! /bin/sh

if [[ "$1" == "" ]]; then
    echo "usage: ./release.sh 0.0.1"
    echo "current releases: "
    git fetch --tags origin 
    exit 1
fi

mvn versions:set -DnewVersion="$1"
rm pom.xml.versionsBackup
git add pom.xml
git commit -m "version bump $1 [skip ci]"
git push
gh release create "v$1" --notes "" 