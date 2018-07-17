#!/usr/bin/env bash

scriptName=`basename "$0"`

usage="Usage: $scriptName [options]

    -h , --help           show this help text
    -i <sourceImage>      a source image to use
                          (e.g. quay.io/alfresco/ags-share-community)
    -r <repository>       a repository to push new tags to
                          (e.g. registry.hub.docker.com)
    -t <tag>              the existing tag for the images (mandatory)
    -d <digestLength>     the length of digest to output (default 12 chars)"

digestLength=12

while getopts ':hi:r:t:d:' option; do
  case "$option" in
    h) echo -e "Tag one or more images to include the digest and push this to some repositories.\n\n${usage}"
       exit
       ;;
    i) sourceImages+=("$OPTARG")
       ;;
    r) repositories+=("$OPTARG")
       ;;
    t) existingTag=$OPTARG
       ;;
    d) digestLength=$OPTARG
       ;;
    :) echo -e "Missing argument for -${OPTARG}\n\n${usage}" >&2
       exit 1
       ;;
   \?) echo -e "Illegal option: -${OPTARG}\n\n${usage}" >&2
       exit 1
       ;;
  esac
done
shift $((OPTIND - 1))

if [ "#$existingTag" == "#" ]; then
    echo -e "Please supply a tag with the -t option.\n\n${usage}" >&2
    exit 1
fi

for sourceImage in ${sourceImages[@]}
do
    echo "Processing $sourceImage"

    # Note that this command should work even if the image is already present locally.
    digest=`docker pull ${sourceImage}:${existingTag} | grep "Digest:" | awk -F':' '{print $3}' | cut -c 1-$digestLength`

    if [ ${#digest} != $digestLength ]
    then
        echo "Unexpected length for digest of ${sourceImage}: '${digest}'" >&2
        exit 1
    fi

    newTag=${existingTag}_${digest}

    # Remove the source repository name if it contains one.
    slashes=`echo $sourceImage | sed "s|[^/]||g"`
    if [ ${#slashes} == 2 ]
    then
        # The repository name is everything up to the first slash.
        image=`echo $sourceImage | sed "s|[^/]*/||"`
    else
        # Assume the source image doesn't reference the repository name.
        image=$sourceImage
    fi

    for repository in ${repositories[@]}
    do
        docker tag ${sourceImage}:${existingTag} ${repository}:${image}:${newTag}
        docker push ${repository}:${image}:${newTag}
        echo "Pushed ${sourceImage}:${existingTag} to ${repository}:${image}:${newTag}"
    done
done
