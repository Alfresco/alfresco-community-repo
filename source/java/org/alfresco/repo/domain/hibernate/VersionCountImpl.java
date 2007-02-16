/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.VersionCount;

/**
 * Hibernate-specific implementation of the domain entity <b>versioncounter</b>.
 * 
 * @author Derek Hulley
 */
public class VersionCountImpl implements VersionCount, Serializable
{
    private static final long serialVersionUID = 6420375860928877809L;

    private StoreKey key;
    @SuppressWarnings("unused")
    private long version;    // used by Hibernate for concurrency
    private int versionCount;

    public VersionCountImpl()
    {
        versionCount = 0;
    }
    
    /**
     * @see #getKey()
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (obj == this)
        {
            return true;
        }
        else if (!(obj instanceof VersionCount))
        {
            return false;
        }
        VersionCount that = (VersionCount) obj;
        return (this.getKey().equals(that.getKey()));
    }
    
    /**
     * @see #getKey()
     */
    public int hashCode()
    {
        return getKey().hashCode();
    }
    
    /**
     * @see #getKey()
     */
    public String toString()
    {
        return getKey().toString();
    }

    public StoreKey getKey() {
		return key;
	}

	public synchronized void setKey(StoreKey key)
    {
		this.key = key;
	}
    
    /**
     * For Hibernate use
     */
    public void setVersionCount(int versionCount)
    {
        this.versionCount = versionCount;
    }

    public int incrementVersionCount()
    {
        int versionCount = getVersionCount() + 1;
        setVersionCount(versionCount);
        return versionCount;
    }

    /**
     * Reset back to 0
     */
    public void resetVersionCount()
    {
        setVersionCount(0);
    }

    public int getVersionCount()
    {
        return versionCount;
    }
}