/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.cluster;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A do-nothing implementation of the {@link Messenger} interface.
 * 
 * @author Matt Ward
 */
public class NullMessenger<T extends Serializable> implements Messenger<T>
{
    private static final Log logger = LogFactory.getLog(NullMessenger.class);
    
    @Override
    public void send(T message)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Throwing away message: " + message);
        }
    }

    @Override
    public void setReceiver(MessageReceiver<T> receiver)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Throwing away receiver: " + receiver);
        }
    }

    @Override
    public boolean isConnected()
    {
        return false;
    }

    @Override
    public String getAddress()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("getAddress() always returns loopback address: 127.0.0.1");
        }
        return "127.0.0.1";
    }
}
