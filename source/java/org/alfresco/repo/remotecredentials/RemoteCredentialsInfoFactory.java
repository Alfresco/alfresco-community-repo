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
package org.alfresco.repo.remotecredentials;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The interface which controls how implementations of 
 *  {@link BaseCredentialsInfo} are serialized
 * 
 * @author Nick Burch
 * @since Odin
 */
public interface RemoteCredentialsInfoFactory
{
    /**
     * Creates a new {@link BaseCredentialsInfo} object of the appropriate
     *  type, based on the details of the underlying node.
     *
     * @param type The type of the credentials node, a child of rc:credentialBase
     * @param nodeRef The NodeRef of the credentials node
     * @param properties All the node properties
     */
    public BaseCredentialsInfo createCredentials(QName type, NodeRef nodeRef, String remoteSystemName, 
            NodeRef remoteSystemContainerNodeRef, Map<QName,Serializable> properties);

    /**
     * Serializes the given {@link BaseCredentialsInfo} object to node properties.
     * 
     * @param info The Credentials object to serialize
     * @return The properties to be serialized for the node
     */
    public Map<QName,Serializable> serializeCredentials(BaseCredentialsInfo info);
    
    /**
     * Helper class for implementations of {@link RemoteCredentialsInfoFactory}
     */
    public static class FactoryHelper
    {
        /**
         * Sets the core properties on a {@link AbstractCredentialsImpl} 
         */
        public static void setCoreCredentials(AbstractCredentialsImpl credentials, Map<QName,Serializable> properties)
        {
            credentials.setRemoteUsername(
                    (String)properties.get(RemoteCredentialsModel.PROP_REMOTE_USERNAME)
            );
            
            Boolean succeeded = (Boolean)properties.get(RemoteCredentialsModel.PROP_LAST_AUTHENTICATION_SUCCEEDED);
            if (succeeded != null)
            {
                credentials.setLastAuthenticationSucceeded(succeeded.booleanValue());
            }
            else
            {
                // Default is that it did
                credentials.setLastAuthenticationSucceeded(true);
            }
        }
        
        /**
         * Generates the core properties for a {@link BaseCredentialsInfo}
         */
        public static Map<QName,Serializable> getCoreCredentials(BaseCredentialsInfo credentials)
        {
            Map<QName,Serializable> properties = new HashMap<QName, Serializable>();
            properties.put(RemoteCredentialsModel.PROP_REMOTE_USERNAME, credentials.getRemoteUsername());
            properties.put(RemoteCredentialsModel.PROP_LAST_AUTHENTICATION_SUCCEEDED, credentials.getLastAuthenticationSucceeded());
            return properties;
        }
    }
}