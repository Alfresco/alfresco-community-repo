/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
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

    private static final String DEFAULT_SOURCE = "direct-vm:alfresco.events";
    private static final String DEFAULT_TARGET = "amqp:topic:alfresco.repo.events?jmsMessageType=Text";

    @Value("${messaging.events.repo.node.sourceQueue.endpoint:" + DEFAULT_SOURCE + "}")
    public String sourceQueue;

    @Value("${messaging.events.repo.node.targetTopic.endpoint:" + DEFAULT_TARGET + "}")
    public String targetTopic;

    @Override
    public void configure() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Repo node events routes config: ");
            logger.debug("SourceQueue is "+sourceQueue);
            logger.debug("targetTopic is "+targetTopic);
        }

        from(sourceQueue).routeId("alfresco.events -> topic:alfresco.repo.events")
        .marshal("defaultDataFormat").to(targetTopic)
        .end();
    }
}
