/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.messaging.camel.routes;

import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Route builder for Repo node events
 * 
 * @author sglover
 */
@Component
public class RepoNodeEventsRouteBuilder extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(RepoNodeEventsRouteBuilder.class);
    
    @Value("${messaging.events.repo.node.sourceQueue.endpoint}")
    public String sourceQueue = "direct-vm:alfresco.events"; //defaults to an invalid notset value
    
    @Value("${messaging.events.repo.node.targetTopic.endpoint}")
    public String targetTopic = "amqp:topic:alfresco.repo.events?jmsMessageType=Text"; //defaults to an invalid notset value

    @Value("${messaging.events.deadletter.endpoint}")
    public String deadLetter = "seda:error"; //defaults to seda error

    @Value("${messaging.routing.numThreads}")
    private int numThreads;

    @Override
    public void configure() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Repo node events routes config: ");
            logger.debug("SourceQueue is "+sourceQueue);
            logger.debug("targetTopic is "+targetTopic);
        }

        errorHandler(deadLetterChannel(deadLetter));

        from(sourceQueue).routeId("alfresco.events -> topic:alfresco.repo.events")
        .threads(numThreads)
        .marshal("defaultDataFormat").to(targetTopic)
        .end();
    }
}
