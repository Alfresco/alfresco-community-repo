/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.client;

import org.apache.chemistry.opencmis.client.api.Session;

/**
 * Represents a CMIS connection.
 */
public interface CMISConnection
{
    /**
     * Gets connection id.
     * 
     * @return connection id
     */
    String getId();

    /**
     * Gets OpenCMIS Session.
     * 
     * @return OpenCMIS session
     */
    Session getSession();

    /**
     * Gets the CMIS Server.
     * 
     * @return CMIS Server
     */
    CMISServer getServer();

    /**
     * Gets the user name.
     * 
     * @return user name
     */
    String getUserName();

    /**
     * Indicates if the connection is a local or a remote connection.
     */
    boolean isLocal();

    /**
     * Releases the CMIS session and removes the connection from connection
     * manager.
     */
    void close();
}
