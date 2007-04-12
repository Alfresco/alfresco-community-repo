/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.service.cmr.avm.deploy;

import java.io.Serializable;

import org.alfresco.util.Pair;

/**
 * Interface for Deployment Events.
 * @author britt
 */
public class DeploymentEvent implements Serializable 
{
    private static final long serialVersionUID = 2696116904379321786L;

    /**
     * The type of the event.
     * @author britt
     */
    public static enum Type implements Serializable
    {
        COPIED,    // Copied a source node that did not exist on the destination.
        UPDATED,   // Overwrote the destination.
        DELETED,   // Deleted the destination node.
        START,     // A Deployment has begun.
        END        // A Deployment has ended.
    };
    
    private Type fType;
    
    private Pair<Integer, String> fSource;
    
    private String fDestination;

    public DeploymentEvent(Type type, Pair<Integer, String> source, String destination)
    {
        fType = type;
        fSource = source;
        fDestination = destination;
    }
    
    /**
     * Get the type of the event.
     * @return The type.
     */
    public Type getType()
    {
        return fType;
    }
    
    /**
     * Get the source node version and path.
     * @return
     */
    public Pair<Integer, String> getSource()
    {
        return fSource;
    }

    /**
     * Get the destination path.
     * @return
     */
    public String getDestination()
    {
        return fDestination;
    }
    
    /**
     * Get a String representation.
     */
    public String toString()
    {
        return fType + ": " + fSource + " -> " + fDestination;
    }
}
