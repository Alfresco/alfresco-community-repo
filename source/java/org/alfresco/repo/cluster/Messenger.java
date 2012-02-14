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

/**
 * Provides facilities for peer-to-peer messaging within a cluster. This interface
 * is intended to act as a facade, allowing the actual implementation (e.g. JGroups
 * or Hazelcast) to be decoupled as much as possible from the Alfresco code base.
 * <p>
 * Instances of this class are parameterised with the type of message payload
 * to send and receive.
 * 
 * @author Matt Ward
 */
public interface Messenger<T extends Serializable>
{
    void send(T message);
    
    void setReceiver(MessageReceiver<T> receiver);
    
    boolean isConnected();
    
    String getAddress();
}
