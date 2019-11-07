/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listener for transform request events.
 *
 * @author aepure
 */
@Component
public class TransformRequestConsumer extends SpringRouteBuilder
{
    private static Log logger = LogFactory.getLog(TransformRequestConsumer.class);

    @Value("${acs.repo.transform.request.endpoint}")
    public String sourceQueue;

    @Autowired
    @Qualifier("transformRequestProcessor")
    private Processor processor;

    // Not restricted for now, should be restricted after performance tests.
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public void setSourceQueue(String sourceQueue)
    {
        this.sourceQueue = sourceQueue;
    }

    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    public void setProcessor(Processor processor)
    {
        this.processor = processor;
    }

    @Override public void configure()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Transform Request events route config: ");
            logger.debug("SourceQueue is " + sourceQueue);
        }

        from(sourceQueue).threads().executorService(executorService).process(processor).end();
    }
}
