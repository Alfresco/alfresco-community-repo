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
package org.alfresco.repo.version.common.counter.hibernate;

import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.VersionCount;
import org.alfresco.repo.domain.hibernate.VersionCountImpl;
import org.alfresco.repo.version.common.counter.VersionCounterDaoService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Version counter DAO service implemtation using Hibernate.
 * <p>
 * The object should execute within its own transaction, and is limited to single-thread
 * entry.  If it becomes a bottleneck, the transaction synchronization should be moved
 * over to reentrant locks and/or the hibernate mappings should be optimized for better
 * read-write access. 
 * 
 * @author Derek Hulley
 */
public class HibernateVersionCounterDaoServiceImpl extends HibernateDaoSupport implements VersionCounterDaoService
{
    /**
     * Retrieves or creates a version counter
     * 
     * @param storeKey
     * @return Returns a current or new version counter
     */
    private VersionCount getVersionCounter(StoreRef storeRef)
    {
        StoreKey storeKey = new StoreKey(storeRef.getProtocol(), storeRef.getIdentifier());
        // get the version counter
        VersionCount versionCounter = (VersionCount) getHibernateTemplate().get(VersionCountImpl.class, storeKey);
        // check if it exists
        if (versionCounter == null)
        {
            // create a new one
            versionCounter = new VersionCountImpl();
            versionCounter.setKey(storeKey);
            getHibernateTemplate().save(versionCounter);
        }
        return versionCounter;
    }
    
    /**
     * Get the next available version number for the specified store.
     * 
     * @param storeRef  the version store id
     * @return          the next version number
     */
    public synchronized int nextVersionNumber(StoreRef storeRef)
    {
        // get the version counter
        VersionCount versionCounter = getVersionCounter(storeRef);
        // get an incremented count
        return versionCounter.incrementVersionCount();
    }
    
    /**
     * Gets the current version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the current version number, zero if no version yet allocated.
     */
    public synchronized int currentVersionNumber(StoreRef storeRef)
    {
        // get the version counter
        VersionCount versionCounter = getVersionCounter(storeRef);
        // get an incremented count
        return versionCounter.getVersionCount();
    }
    
    /**
     * Resets the version number for a the specified store.
     * 
     * WARNING: calling this method will completely reset the current 
     * version count for the specified store and cannot be undone.  
     *
     * @param storeRef  the store reference
     */
    public synchronized void resetVersionNumber(StoreRef storeRef)
    {
        // get the version counter
        VersionCount versionCounter = getVersionCounter(storeRef);
        // get an incremented count
        versionCounter.resetVersionCount();
    }
}
