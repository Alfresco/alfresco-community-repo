package org.alfresco.repo.transfer;
/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * Data Transfer Object for a TransferTarget.   The definition of the connection to a remote system. 
 *
 * @author Mark Rogers
 */
public class TransferTargetImpl implements TransferTarget
{
    private NodeRef nodeRef;
    private String name;
    private String title;
    private String description;
    private String endpointProtocol;
    private String endpointHost; 
    private int endpointPort;
    private String endpointPath = "/alfresco/service/api/transfer";
    private String username;
    private char[] password;
    private boolean enabled;
    
    public void setNodeRef(NodeRef nodeRef)
    {
        this.nodeRef = nodeRef;
    }
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getTitle()
    {
        return title;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }
    public void setEndpointProtocol(String endpointProtocol)
    {
        this.endpointProtocol = endpointProtocol;
    }
    public String getEndpointProtocol()
    {
        return endpointProtocol;
    }
    public void setEndpointHost(String endpointHost)
    {
        this.endpointHost = endpointHost;
    }
    public String getEndpointHost()
    {
        return endpointHost;
    }
    public void setPassword(char[] password)
    {
        this.password = password;
    }
    public char[] getPassword()
    {
        return password;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public String getUsername()
    {
        return username;
    }
    public void setEndpointPath(String endpointPath)
    {
        this.endpointPath = endpointPath;
    }
    public String getEndpointPath()
    {
        return endpointPath;
    }
    public void setEndpointPort(int endpointPort)
    {
        this.endpointPort = endpointPort;
    }
    public int getEndpointPort()
    {
        return endpointPort;
    }
    
    /**
     * @see #getNodeRef()
     * @see NodeRef#equals(Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (this == obj)
        {
            return true;
        }
        else if (obj instanceof TransferTargetImpl == false)
        {
            return false;
        }
        TransferTargetImpl that = (TransferTargetImpl) obj;
        if(this.getNodeRef() == null)
        {
            return (this.getName().equals(that.getName()));
        }
        else
        {
            return (this.getNodeRef().equals(that.getNodeRef()));
        }
    }

    /**
     * @see #getNodeRef()
     * @see NodeRef#hashCode()
     */
    public int hashCode()
    {
        if(nodeRef == null)
        {
            return 0;
        }
        return getNodeRef().hashCode();
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public String toString()
    {
        return "TransferTarget: " + name + ",host:" + endpointHost + ",port:" + endpointPort;
    }
}
