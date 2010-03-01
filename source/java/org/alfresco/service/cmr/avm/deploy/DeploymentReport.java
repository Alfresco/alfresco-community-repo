/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.service.cmr.avm.deploy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A value class that contains the results of a deployment run.
 * @author britt
 */
public class DeploymentReport implements Serializable, Iterable<DeploymentEvent>
{
    private static final long serialVersionUID = 5528250199836250533L;
    
    private List<DeploymentEvent> fEvents;
    
    public DeploymentReport()
    {
        fEvents = new ArrayList<DeploymentEvent>();
    }
    
    /**
     * Get a String representation.
     */
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (DeploymentEvent event : fEvents)
        {
            builder.append(event.toString());
            builder.append('\n');
        }
        return builder.toString();
    }
    
    /**
     * Add an event.
     * @param event
     */
    public void add(DeploymentEvent event)
    {
        fEvents.add(event);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<DeploymentEvent> iterator() 
    {
        return fEvents.iterator();
    }
    
    public List<DeploymentEvent> getEvents()
    {
    	return fEvents;
    }
    
}
