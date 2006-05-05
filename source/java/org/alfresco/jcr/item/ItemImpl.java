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

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.alfresco.jcr.session.SessionImpl;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;


/**
 * Alfresco Implementation of an Item
 * 
 * @author David Caruana
 */
public abstract class ItemImpl implements Item
{
    protected SessionImpl session;
    

    /**
     * Construct
     * 
     * @param session
     */
    public ItemImpl(SessionImpl session)
    {
        this.session = session;
    }
    
    /**
     * Get the Session implementation
     * 
     * @return session implementation
     */
    public SessionImpl getSessionImpl()
    {
        return session;
    }
    
    /**
     * Get the Item Proxy
     * 
     * @return  the proxy
     */
    public abstract Item getProxy();
    
    /* (non-Javadoc)
     * @see javax.jcr.Item#getSession()
     */
    public Session getSession() throws RepositoryException
    {
        return session.getProxy();
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.Item#isNew()
     */
    public boolean isNew()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#isModified()
     */
    public boolean isModified()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#save()
     */
    public void save() throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException, ReferentialIntegrityException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException
    {
        AlfrescoTransactionSupport.flush();
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    
}
