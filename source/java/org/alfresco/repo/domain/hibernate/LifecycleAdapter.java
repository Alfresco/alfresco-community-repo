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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.error.AlfrescoRuntimeException;
import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;

/**
 * Helper base class providing lifecycle and other support
 * 
 * @author Derek Hulley
 */
public abstract class LifecycleAdapter implements Lifecycle
{
    /** Helper */
    private Session session;
    
    /**
     * @return Returns the session that this object was used in
     */
    protected Session getSession()
    {
        if (session == null)
        {
            throw new AlfrescoRuntimeException("Hibernate entity is not part of a session: " + this);
        }
        return session;
    }

    /**
     * @return Returns NO_VETO always
     */
    public boolean onDelete(Session session) throws CallbackException
    {
        return NO_VETO;
    }
    
    /** NO OP */
    public void onLoad(Session session, Serializable id)
    {
        this.session = session;
    }

    /** @return Returns NO_VETO always */
    public boolean onSave(Session session) throws CallbackException
    {
        this.session = session;
        return NO_VETO;
    }

    /** @return Returns NO_VETO always */
    public boolean onUpdate(Session session) throws CallbackException
    {
        this.session = session;
        return NO_VETO;
    }
}
