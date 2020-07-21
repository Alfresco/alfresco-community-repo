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
public class Shard implements Serializable
{
    private static final long serialVersionUID = -7255962796619754211L;

    private Floc floc;
    
    private int instance;

    public Shard()
    {
        
    }
    
    /**
     * @return the floc
     */
    public Floc getFloc()
    {
        return floc;
    }

    /**
     * @param floc the floc to set
     */
    public void setFloc(Floc floc)
    {
        this.floc = floc;
    }

    /**
     * @return the instance
     */
    public int getInstance()
    {
        return instance;
    }

    /**
     * @param instance the instance to set
     */
    public void setInstance(int instance)
    {
        this.instance = instance;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((floc == null) ? 0 : floc.hashCode());
        result = prime * result + instance;
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
        Shard other = (Shard) obj;
        if (floc == null)
        {
            if (other.floc != null)
                return false;
        }
        else if (!floc.equals(other.floc))
            return false;
        if (instance != other.instance)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Shard [floc=" + floc + ", instance=" + instance + "]";
    }

   

}
