/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.domain.hibernate;

import org.alfresco.repo.domain.Node;
import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.VersionCount;

/**
 * Hibernate-specific implementation of the domain entity <b>versioncounter</b>.
 * 
 * @author Derek Hulley
 */
public class VersionCountImpl implements VersionCount
{
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
        else if (!(obj instanceof Node))
        {
            return false;
        }
        Node that = (Node) obj;
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
    private void setVersionCount(int versionCount)
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