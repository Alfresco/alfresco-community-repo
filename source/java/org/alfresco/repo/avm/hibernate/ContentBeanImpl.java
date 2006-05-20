/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm.hibernate;


/**
 * Shared Content between files.
 * @author britt
 */
public class ContentBeanImpl implements ContentBean
{
    /**
     * The object id.
     */
    private long fID;
    
    /**
     * The reference count of this id.
     */
    private int fRefCount;
    
    /**
     * The version (for concurrency control).
     */
    private long fVers;
    
    /**
     * Default constructor.
     */
    public ContentBeanImpl()
    {
    }

    /**
     * Basic constructor with an id.
     */
    public ContentBeanImpl(long id)
    {
        fID = id;
        fRefCount = 0;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Content#setId(int)
     */
    public void setId(long id)
    {
        fID = id;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Content#getID()
     */
    public long getId()
    {
        return fID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Content#setRefCount(int)
     */
    public void setRefCount(int refCount)
    {
        fRefCount = refCount;
    }

    /* (non-Javadoc)
     * @see org.alfresco.proto.avm.Content#getRefCount()
     */
    public int getRefCount()
    {
        return fRefCount;
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
        if (!(obj instanceof ContentBean))
        {
            return false;
        }
        return fID == ((ContentBean)obj).getId();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (int)fID;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.ContentBean#getVers()
     */
    public long getVers()
    {
        return fVers;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.hibernate.ContentBean#setVers(java.lang.int)
     */
    public void setVers(long vers)
    {
        fVers = vers;
    }
}
