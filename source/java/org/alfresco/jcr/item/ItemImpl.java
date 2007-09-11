/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
    }

    /* (non-Javadoc)
     * @see javax.jcr.Item#refresh(boolean)
     */
    public void refresh(boolean keepChanges) throws InvalidItemStateException, RepositoryException
    {
        throw new UnsupportedRepositoryOperationException();
    }

    
}
