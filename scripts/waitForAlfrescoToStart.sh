#!/usr/bin/env bash

WAIT_INTERVAL=1
COUNTER=0
TIMEOUT=300
t0=`date +%s`

echo "Waiting for alfresco to start"
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/alfresco) || [ "$COUNTER" -eq "$TIMEOUT" ]; do
   printf '.'
   sleep $WAIT_INTERVAL
   COUNTER=$(($COUNTER+$WAIT_INTERVAL))
done

if (("$COUNTER" < "$TIMEOUT")) ; then
   t1=`date +%s`
   delta=$((($t1 - $t0)/60))
   echo "Alfresco Started in $delta minutes"
else
   echo "Waited $COUNTER seconds"
   echo "Alfresco Could not start in time."
   exit 1
fi