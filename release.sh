#! /bin/sh

if [[ "$1" == "" ]]; then
    echo "usage: ./release.sh v0.0.1"
    echo "current releases: "
    git fetch --tags origin 
    exit 1
fi

gh release create "$1" --notes "" 