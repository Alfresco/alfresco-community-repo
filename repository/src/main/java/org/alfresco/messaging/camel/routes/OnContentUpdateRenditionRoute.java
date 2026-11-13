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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.alfresco.model.RenditionModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.util.PostTxnCallbackScheduler;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Rendition listener for on content update raw event.
 * 
 * @author Cristian Turlica
 */
@Component
public class OnContentUpdateRenditionRoute extends RouteBuilder implements ContentServicePolicies.OnContentUpdatePolicy
{
    private static Log logger = LogFactory.getLog(OnContentUpdateRenditionRoute.class);

    @Value("${acs.repo.rendition.events.endpoint}")
    public String sourceQueue;

    // Not restricted for now, should be restricted after performance tests.
    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    private PolicyComponent policyComponent;

    @Autowired
    private PostTxnCallbackScheduler renditionRequestSheduler;

    @Autowired
    private RenditionService2Impl renditionService2;

    @Override
    public void configure() throws Exception
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("OnContentUpdate rendition events route config: ");
            logger.debug("SourceQueue is " + sourceQueue);
        }

        policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, RenditionModel.ASPECT_RENDITIONED,
                new JavaBehaviour(this, "onContentUpdate", Behaviour.NotificationFrequency.EVERY_EVENT));

        // Only consumes messages sent by earlier versions, which sent each content update to this queue.
        from(sourceQueue).threads().executorService(executorService).process("renditionEventProcessor").end();
    }

    @Override
    public void onContentUpdate(NodeRef sourceNodeRef, boolean newContent)
    {
        // Re-render after commit on a background thread, without a round trip through the message broker.
        renditionRequestSheduler.scheduleRendition(() -> {
            renditionService2.onContentUpdate(sourceNodeRef, newContent);
            return null;
        }, "onContentUpdate:" + sourceNodeRef);
    }
}
