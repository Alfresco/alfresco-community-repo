#!/usr/bin/env bash

export ALFRESCO_URL=$1
export EXTRA_WAIT_INTERVAL=$2

if [ -z "$ALFRESCO_URL" ]
then
  echo "Please provide the Alfresco URL to check, for example: \"${0##*/} http://localhost:8080/alfresco\""
  exit 1
fi

# If MTLS enabled configure keystore/truststore for curl command
if [[ $ALFRESCO_URL == https* ]]; then
  KEYSTORE_TRUSTSTORE_PATH="${CI_WORKSPACE}/keystores/testClient"
  KEYSTORE_PASSWORD="password"

  ADDITIONAL_MTLS_CONFIG="--key $KEYSTORE_TRUSTSTORE_PATH/client-key.pem --cert $KEYSTORE_TRUSTSTORE_PATH/client-cert.pem:$KEYSTORE_PASSWORD --cacert $KEYSTORE_TRUSTSTORE_PATH/testClient_truststore.pem"
  if [[ ${HOSTNAME_VERIFICATION_DISABLED} == true ]]; then
    ADDITIONAL_MTLS_CONFIG=$ADDITIONAL_MTLS_CONFIG" -k"
  fi
else
  ADDITIONAL_MTLS_CONFIG=""
fi

WAIT_INTERVAL=1
COUNTER=0
TIMEOUT=300
t0=$(date +%s)

echo "Waiting for alfresco to start"
echo curl --output /dev/null --silent --head --fail ${ADDITIONAL_MTLS_CONFIG} ${ALFRESCO_URL}
until $(curl --output /dev/null --silent --head --fail ${ADDITIONAL_MTLS_CONFIG} ${ALFRESCO_URL}) || [ "$COUNTER" -eq "$TIMEOUT" ]; do
   printf '.'
   sleep $WAIT_INTERVAL
   COUNTER=$(($COUNTER+$WAIT_INTERVAL))
done

if (("$COUNTER" < "$TIMEOUT")) ; then
   t1=$(date +%s)
   delta=$((($t1 - $t0)/60))
   echo "Alfresco Started in $delta minutes"

   if [ -n "$EXTRA_WAIT_INTERVAL" ]
   then
      echo "Waiting an extra $EXTRA_WAIT_INTERVAL for all the containers to initialise..."
      sleep $EXTRA_WAIT_INTERVAL
      echo "Waited $EXTRA_WAIT_INTERVAL seconds"
   fi
else
   echo "Waited $COUNTER seconds"
   echo "Alfresco Could not start in time."
   echo "All started containers:"	
   docker ps -a	
   ALFCONTAINER=`docker ps -a | grep _alfresco | awk '{ print $1 }'`
   echo "Last 200 lines from alfresco.log on container $ALFCONTAINER:"
   docker logs --tail=200 $ALFCONTAINER
   exit 1
fi