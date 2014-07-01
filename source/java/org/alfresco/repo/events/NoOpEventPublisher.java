/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.events;

import org.alfresco.events.types.BrowserEvent;
import org.alfresco.events.types.Event;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * An implementation of EventPublisher that does nothing.
 *
 * @author Gethin James
 * @since 5.0
 */
public class NoOpEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public void publishEvent(Event event)
    {
    	if (logger.isDebugEnabled())
    	{
            logger.debug("No event published for [" + event + "]");
        }
    }

    @Override
    public void publishEvent(EventPreparator prep)
    {
    	if (logger.isDebugEnabled())
    	{
            logger.debug("No event published with preparator.");
        }
    }

    @Override
    public void publishBrowserEvent(final WebScriptRequest req, final String siteId, final String component, final String action, final String attributes)
    {
        publishEvent(new EventPreparator(){
            @Override
            public Event prepareEvent(String user, String networkId, String transactionId)
            {
                String agent = req.getHeader("user-agent");
                return new BrowserEvent(user, networkId, transactionId, siteId, component, action, agent, attributes);
            }
        });
    }
	
}