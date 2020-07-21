/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.index.shard;

import java.io.Serializable;


/**
 * @author Andy
 *
 */
public class ShardInstance implements Serializable
{
    private static final long serialVersionUID = -3407675092111933581L;

    private Shard shard;
    
    private String baseUrl;
    
    private int port;
    
    private String hostName;
   
    public ShardInstance()
    {
    }
    
    /**
     * @return the shard
     */
    public Shard getShard()
    {
        return shard;
    }

    /**
     * @param shard the shard to set
     */
    public void setShard(Shard shard)
    {
        this.shard = shard;
    }

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl)
    {
    	if((baseUrl != null) && baseUrl.endsWith("/"))
    	{
    		this.baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    	}
    	else
    	{
        this.baseUrl = baseUrl;
    }
    }

    /**
     * @return the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /**
     * @return the hostName
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + port;
        result = prime * result + ((shard == null) ? 0 : shard.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ShardInstance other = (ShardInstance) obj;
        if (baseUrl == null)
        {
            if (other.baseUrl != null)
                return false;
        }
        else if (!baseUrl.equals(other.baseUrl))
            return false;
        if (hostName == null)
        {
            if (other.hostName != null)
                return false;
        }
        else if (!hostName.equals(other.hostName))
            return false;
        if (port != other.port)
            return false;
        if (shard == null)
        {
            if (other.shard != null)
                return false;
        }
        else if (!shard.equals(other.shard))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ShardInstance [shard=" + shard + ", baseUrl=" + baseUrl + ", port=" + port + ", hostName=" + hostName + "]";
    }

    

}
