#!/usr/bin/env bash

scriptName=`basename "$0"`

if [ "$#" -ne 2 ]; then
    echo "Usage: ${scriptName} <imageName> <existingTag>"
    exit 1
fi

echo "${scriptName} called with:"
image=$1
echo "  image: $image"
existingTag=$2
echo "  existingTag: $existingTag"

DIGEST_LENGTH=12

# Note that this command should work even if the image is already present locally.
digest=`docker pull ${image}:${existingTag} | grep "Digest:" | awk -F':' '{print $3}' | cut -c 1-$DIGEST_LENGTH`

if [ ${#digest} != $DIGEST_LENGTH ]
then
    echo "Unexpected length for digest: '$digest'"
    exit 1
fi

newTag=${existingTag}_${digest}
docker tag ${image}:${existingTag} ${image}:${newTag}
docker push ${image}:${newTag}

echo "Pushed ${image}:${existingTag} to ${image}:${newTag}"
