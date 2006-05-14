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

import org.alfresco.repo.domain.StoreKey;
import org.alfresco.repo.domain.VersionCount;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.common.counter.VersionCounterService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.hibernate.LockMode;
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
public class VersionCounterDaoComponentImpl
        extends HibernateDaoSupport
        implements VersionCounterService, NodeServicePolicies.BeforeCreateStorePolicy
{
    private PolicyComponent policyComponent;

    /**
     * @param policyComponent the component to register behaviour
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * Bind to receive notifications of store creations
     */
    public void init()
    {
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCreateStore"),
                this,
                new JavaBehaviour(this, "beforeCreateStore"));
    }

    /**
     * Create a version counter for the store
     * @param nodeTypeQName
     * @param storeRef
     */
    public void beforeCreateStore(QName nodeTypeQName, StoreRef storeRef)
    {
        final StoreKey storeKey = new StoreKey(storeRef.getProtocol(), storeRef.getIdentifier());
        VersionCount versionCount = (VersionCount) getHibernateTemplate().get(VersionCountImpl.class, storeKey);
        if (versionCount != null)
        {
            // already exists
            return;
        }
        versionCount = new VersionCountImpl();
        versionCount.setKey(storeKey);
        getHibernateTemplate().save(versionCount);
    }

    /**
     * Retrieves or creates a version counter.  This locks the counter against updates for the
     * current transaction.
     * 
     * @param storeKey the primary key of the counter
     * @return Returns a current or new version counter
     */
    private VersionCount getVersionCounter(StoreRef storeRef)
    {
        final StoreKey storeKey = new StoreKey(storeRef.getProtocol(), storeRef.getIdentifier());
        
        // check if it exists
        VersionCount versionCount = (VersionCount) getHibernateTemplate().get(
                VersionCountImpl.class,
                storeKey,
                LockMode.UPGRADE);
        if (versionCount == null)
        {
            // This could fail on some databases with concurrent adds.  But it is only those databases
            // that don't lock the index against an addition of the row, and then it will only fail once.
            versionCount = new VersionCountImpl();
            versionCount.setKey(storeKey);
            getHibernateTemplate().save(versionCount);
            // debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Created version counter: \n" +
                        "   Thread: " + Thread.currentThread().getName() + "\n" +
                        "   Version count: " + versionCount.getVersionCount());
            }
        }
        else
        {
            // debug
            if (logger.isDebugEnabled())
            {
                logger.debug("Got version counter: \n" +
                        "   Thread: " + Thread.currentThread().getName() + "\n" +
                        "   Version count: " + versionCount.getVersionCount());
            }
        }
        // done
        return versionCount;
    }
    
    /**
     * Get the next available version number for the specified store.
     * 
     * @param storeRef  the version store id
     * @return          the next version number
     */
    public int nextVersionNumber(StoreRef storeRef)
    {
        // get the version counter
        VersionCount versionCount = getVersionCounter(storeRef);
        // get an incremented count
        int nextCount = versionCount.incrementVersionCount();
        
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Incremented version count: \n" +
                    "   Thread: " + Thread.currentThread().getName() + "\n" +
                    "   New version count: " + versionCount.getVersionCount());
        }
        return nextCount;
    }
    
    /**
     * Gets the current version number for the specified store.
     * 
     * @param storeRef  the store reference
     * @return          the current version number, zero if no version yet allocated.
     */
    public int currentVersionNumber(StoreRef storeRef)
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
    
    /**
     * Sets the version number for a specified store.
     * 
     * WARNING: calling this method will completely reset the current 
     * version count for the specified store and cannot be undone.  
     *
     * @param storeRef  the store reference
     * @param versionCount  the new version count
     */
    public synchronized void setVersionNumber(StoreRef storeRef, int versionCount)
    {
        // get the version counter
        VersionCount versionCounter = getVersionCounter(storeRef);
        // get an incremented count
        versionCounter.setVersionCount(versionCount);
    }
    
}
