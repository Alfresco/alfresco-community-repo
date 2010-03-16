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
        CREATED,   // Copied a source node that did not exist on the destination.
        UPDATED,   // Overwrote the destination.
        DELETED,   // Deleted the destination node.
        START,     // A Deployment has begun.
        END,       // A Deployment has ended.
        FAILED     // A Deployment failed.
    };

    private Type fType;

    private Pair<Integer, String> fSource;

    private String fDestination;
    
    private String fMessage;

    public DeploymentEvent(Type type, Pair<Integer, String> source, String destination)
    {
        fType = type;
        fSource = source;
        fDestination = destination;
    }
    
    public DeploymentEvent(Type type, Pair<Integer, String> source, String destination, String message)
    {
        this(type, source, destination);
        
        fMessage = message;
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
     * Get the message.
     * @return
     */
    public String getMessage()
    {
        return fMessage;
    }

    /**
     * Get a String representation.
     */
    public String toString()
    {
        String str = fType + ": " + fSource + " -> " + fDestination;
        
        if (fMessage != null)
        {
           str = str + " (" + fMessage + ")";
        }
        
        return str;
    }
    
    /**
     * 
     */
    public int hashCode()
    {
    	return (fType.toString() + fDestination).hashCode();
    }
    
    public boolean equals(Object obj)
    {
    	if(obj instanceof DeploymentEvent) 
    	{
    		DeploymentEvent other = (DeploymentEvent)obj;
    		if(this.getType() == other.getType() && this.getDestination().equals(other.getDestination()))
    		{
    			// objects are equal
    			return true;
    		}	
    	}
    	return false;
    }
}
