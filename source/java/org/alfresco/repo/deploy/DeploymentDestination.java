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
package org.alfresco.repo.deploy;

/**
 * Class to hold Deployment destination information.
 * Used as a lock to serialize deployments to the same
 * destination.
 * @author britt
 */
public class DeploymentDestination
{
    private String fHost;

    private int fPort;

    DeploymentDestination(String host, int port)
    {
        fHost = host;
        fPort = port;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof DeploymentDestination))
        {
            return false;
        }
        DeploymentDestination other = (DeploymentDestination)obj;
        return fHost.equals(other.fHost) && fPort == other.fPort;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return fHost.hashCode() + fPort;
    }

    public String toString()
    {
        return fHost;
    }
}

