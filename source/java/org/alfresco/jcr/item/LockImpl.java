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
