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
package org.alfresco.service.cmr.transfer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Transfer Target.    Definition of a remote target to transfer to, contains details such as its name and address.
 *
 * @author Mark Rogers
 */
public interface TransferTarget
{
    /**
     * read only - get the node reference of the underlying transfer target node.
     * @return NodeRef
     */
    public NodeRef getNodeRef();
    
    /**
     * Get the name of this transfer target
     * @return String
     */
    public String getName();
        
    /**
     * Get the description for this transfer target
     * @return String
     */
    public String getDescription();
    
    /**
     * Set the decription for this transfer target
     * @param description String
     */
    public void setDescription(String description);
    
    /**
     * Get the title of this transfer target 
     * @return String
     */
    String getTitle();
    
    /**
     * Set the title for this transfer target
     * @param title String
     */
    public void setTitle(String title);
    
    /**
     * Get the endpoint host
     * @return String
     */
    public String getEndpointHost();
    
    /**
     * Set the endpoint host
     * @param endpointHost String
     */
    public void setEndpointHost(String endpointHost);
    
    /**
     * Get the endpoint port
     * @return int
     */
    int getEndpointPort();
    
    /**
     * Set the endpoint port
     * @param endpointPort int
     */
    public void setEndpointPort(int endpointPort);
    
    /**
     * HTTP OR HTTPS
     */
    public String getEndpointProtocol();
    
    /**
     * Set the endpoint protocol.
     * @param endpointProtocol String
     */
    public void setEndpointProtocol(String endpointProtocol);
    
    /**
     * Set the password for this transfer target
     * @param password clear text password.
     */
    public void setPassword(char[] password);    

    /**
     * The username used to authenticate with the transfer target
     * @return String
     */
    String getUsername();
    
    /**
     * The username used to authenticate with the transfer target
     * @param userName String
     */
    void setUsername(String userName);
    
    /**
     * Get the cleartext password
     * @return char[]
     */
    char[] getPassword();
    
    /**
     * The location of the transfer service on the target endpoint host
     * Defaults to "/alfresco/service/api/transfer", and this shouldn't typically need to change
     * @return String
     */
    String getEndpointPath();
    
    /**
     * The location of the transfer service on the target endpoint host
     * Defaults to "/alfresco/service/api/transfer", and this shouldn't typically need to change
     */
    void setEndpointPath(String path);
    
    /**
     * is this transfer target enabled or disabled?
     */
    boolean isEnabled();
    
    /**
     * enable this transfer target
     */
    void setEnabled(boolean enabled);
}
