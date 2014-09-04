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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.alfresco.events.types.BrowserEvent;
import org.alfresco.events.types.Event;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * An implementation of EventPublisher that is used for testing
 * 
 * @author Gethin James
 * @since 5.0
 */
public class EventPublisherForTestingOnly implements EventPublisher
{
    private static final Logger logger = LoggerFactory.getLogger(EventPublisherForTestingOnly.class);
    Queue<Event> queue = new ConcurrentLinkedQueue<Event>();
    
    @Override
    public void publishEvent(Event event)
    {
        queue.add(event);
        log("Event published for [" + event + "]");
    }

    @Override
    public void publishEvent(EventPreparator prep)
    {
        ThreadInfo info = getThreadInfo();
        Event event = prep.prepareEvent(info.user, info.network, info.transaction);
        queue.add(event);
        log("Event published: [" + event + "]");
    }

//
//    @Override
//    public void publishBrowserEvent(final WebScriptRequest req, final String siteId, final String component, final String action, final String attributes)
//    {
//        publishEvent(new EventPreparator(){
//            @Override
//            public Event prepareEvent(String user, String networkId, String transactionId)
//            {
//                String agent = req.getHeader("user-agent");
//                return new BrowserEvent(user, networkId, transactionId, siteId, component, action, agent, attributes);
//            }
//        });
//    }
    
    private void log (String logMessage)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(logMessage);
        }
    }
    
    public Queue<Event> getQueue()
    {
        return this.queue;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Event> List<T> getQueueByType(Class<T> type)
    {
        List<T> toReturn = new ArrayList<>();
        for (Event event : queue)
        {
           if (type.equals(event.getClass()))
           {
               toReturn.add((T) event);
           }
        }
        return (List<T>) toReturn;
    }
    
    /**
     * Gets userful information from the current thread for use when creating an event
     * @return ThreadInfo
     */
    protected ThreadInfo getThreadInfo()
    {
        return new ThreadInfo(
                    AuthenticationUtil.getFullyAuthenticatedUser(),
                    AlfrescoTransactionSupport.getTransactionId(),
                    TenantUtil.getCurrentDomain());
    }
    
    /**
     * Basic information from a thread
     *
     */
    public static class ThreadInfo {
        public final String user;
        public final String transaction;
        public final String network;
        
        public ThreadInfo(String user, String transaction, String network)
        {
            super();
            this.user = user;
            this.transaction = transaction;
            this.network = network;
        }
    }


}
