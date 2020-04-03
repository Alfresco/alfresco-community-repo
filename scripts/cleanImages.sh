#!/usr/bin/env bash
set -x

echo $imagesToBeDeleted
echo "List all images:"
docker images -a

docker_images_list=$(docker images | grep $imagesToBeDeleted | awk '{print $3}' | uniq)
if [ "$docker_images_list" == "" ]; then
    echo "No docker images on the agent"
else
    echo "Clearing images: $docker_images_list"
    if docker rmi -f $docker_images_list ; then
        echo "Deleting images was successful."
    else
        echo "Deleting specified images failed, so falling back to delete ALL images on system."
        docker_images_list=$(docker images | awk '{print $3}' | uniq)
        docker rmi -f $docker_images_list
    fi
fi
