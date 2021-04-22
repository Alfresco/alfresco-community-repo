#!/usr/bin/env bash
set -xe
WAIT_INTERVAL=1
COUNTER=0
TIMEOUT=300
t0=$(date +%s)

echo "Waiting for alfresco to start"
until $(curl --output /dev/null --silent --head --fail ${ALFRESCO_URL}) || [ "$COUNTER" -eq "$TIMEOUT" ]; do
   printf '.'
   sleep $WAIT_INTERVAL
   COUNTER=$(($COUNTER+$WAIT_INTERVAL))
done

if (("$COUNTER" < "$TIMEOUT")) ; then
   t1=$(date +%s)
   delta=$((($t1 - $t0)/60))
   echo "Alfresco Started in $delta minutes"
else
   echo "Waited $COUNTER seconds"
   echo "Alfresco Could not start in time."
   echo "All started containers:"
   docker ps -a
   ALFCONTAINER=$(docker ps -a | grep _alfresco_1 | awk '{ print $1 }')
   echo "Last 200 lines from alfresco.log on container $ALFCONTAINER:"
   docker logs --tail=200 ${ALFCONTAINER}
   exit 1
fi

