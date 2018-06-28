#!/bin/bash

echo $IMAGES_TO_BE_DELETED
echo "List all images:"
docker images -a

docker_images_list=$(docker images | grep $IMAGES_TO_BE_DELETED | awk '{print $3}' | uniq)
if [ "$docker_images_list" == "" ]; then
    echo "No docker images on the agent"
else
    echo "Clearing images: $docker_images_list"
    docker rmi -f $docker_images_list
fi