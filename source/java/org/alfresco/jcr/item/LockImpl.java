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
package org.alfresco.jcr.item;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.alfresco.jcr.util.JCRProxyFactory;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Alfresco implementation of a JCR Lock
 * 
 * @author David Caruana
 */
public class LockImpl implements Lock
{
    
    private NodeImpl node;
    private Lock proxy = null;

    
    /**
     * Constructor
     *  
     * @param node  node holding lock
     */
    public LockImpl(NodeImpl node)
    {
        this.node = node;
    }

    /**
     * Create proxied JCR Lock
     * 
     * @return  lock
     */
    public Lock getProxy()
    {
        if (proxy == null)
        {
            proxy = (Lock)JCRProxyFactory.create(this, Lock.class, node.session); 
        }
        return proxy;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#getLockOwner()
     */
    public String getLockOwner()
    {
        String lockOwner = null;
        NodeService nodeService = node.session.getRepositoryImpl().getServiceRegistry().getNodeService();
        if (nodeService.hasAspect(node.getNodeRef(), ContentModel.ASPECT_LOCKABLE))
        {
            lockOwner = (String)nodeService.getProperty(node.getNodeRef(), ContentModel.PROP_LOCK_OWNER);
        }
        return lockOwner;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#isDeep()
     */
    public boolean isDeep()
    {
        return false;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#getNode()
     */
    public Node getNode()
    {
        return node.getProxy();
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#getLockToken()
     */
    public String getLockToken()
    {
        LockService lockService = node.session.getRepositoryImpl().getServiceRegistry().getLockService();
        LockStatus lockStatus = lockService.getLockStatus(node.getNodeRef());
        return lockStatus.equals(LockStatus.LOCK_OWNER) ? node.getNodeRef().toString() : null;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#isLive()
     */
    public boolean isLive() throws RepositoryException
    {
        return getLockToken() == null ? false : true;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#isSessionScoped()
     */
    public boolean isSessionScoped()
    {
        return false;
    }

    /*
     *  (non-Javadoc)
     * @see javax.jcr.lock.Lock#refresh()
     */
    public void refresh() throws LockException, RepositoryException
    {
        // note: for now, this is a noop
    }

}
