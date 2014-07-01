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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import org.alfresco.events.types.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of EventPublisher that is used for testing
 * Uses a java.util.logging.FileHandler for logging Events to a file.
 * It also uses the standard log4j logging for Alfresco.
 * 
 * @author Gethin James
 * @since 5.0
 */
public class EventPublisherForTestingOnly extends AbstractEventPublisher implements EventPublisher, InitializingBean
{
    private static final Logger logger = LoggerFactory.getLogger(EventPublisherForTestingOnly.class);
    Queue<Event> queue = new ConcurrentLinkedQueue<Event>();
    FileHandler fileHandler;
    
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

    private void log (String logMessage)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug(logMessage);
        }
        fileHandler.publish(new LogRecord(Level.INFO, logMessage));  
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

    @Override
    public void afterPropertiesSet() throws Exception
    {
        File eventLog = new File("alfresco-events.log");
        fileHandler = new FileHandler(eventLog.getAbsolutePath(), true);
        fileHandler.setLevel(Level.INFO);
        fileHandler.setFormatter(new SimpleFormatter());
    }

}
