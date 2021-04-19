#!/bin/bash

alfresco_docker_image=$1
# Verify release tags
get_tags="$(curl https://hub.docker.com/r/$alfresco_docker_image/tags/ | grep -o '\"result\".*\"]')"
arrayTags=($get_tags)

echo "Existing Tags: $get_tags"

for tag in "${arrayTags[@]}"
do
    if [[ $tag = ${RELEASE_VERSION} ]]; then
        echo "Tag ${RELEASE_VERSION} already pushed, release process will interrupt."
        exit 0
    fi
done

echo "The ${RELEASE_VERSION} tag was not found"
