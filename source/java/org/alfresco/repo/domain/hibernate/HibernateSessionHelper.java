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

import java.util.List;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Utililty support against hibernate sessions. Supported by a super event listener which is registered on the even
 * listener of the hibernate session.
 * 
 * @author andyh
 */
public class HibernateSessionHelper extends HibernateDaoSupport implements HibernateSessionSupport
{
    /**
     * 
     */
    private static final long serialVersionUID = -2532286150392812816L;
    private static final String HIBERNATE_SESSION_EVENT_LISTENER = "HibernateSessionEventListener";

    public void mark()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.mark(getSession());
    }

    public void mark(String label)
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.mark(getSession(), label);
    }

    public void reset()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.reset(getSession());
    }

    public void reset(String label)
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.reset(getSession(), label);
    }
    
    public void removeMark()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.removeMark(getSession());
    }

    public void removeMark(String label)
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.removeMark(getSession(), label);
    }

    public void resetAndRemoveMark()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.resetAndRemoveMark(getSession());
    }

    public void resetAndRemoveMark(String label)
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        resource.resetAndRemoveMark(getSession(), label);
    }

    public List<String> getMarks()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        return resource.getMarks(getSession());
    }
    
    public String getCurrentMark()
    {
        HibernateSessionHelperResourceProvider resource = getResource();
        return resource.getCurrentMark();
    }

    public static HibernateSessionHelperResourceProvider getResource()
    {
        HibernateSessionHelperResourceProvider listener = (HibernateSessionHelperResourceProvider) AlfrescoTransactionSupport.getResource(HIBERNATE_SESSION_EVENT_LISTENER);
        if (listener == null)
        {
            listener = new HibernateSessionHelperResource();
            AlfrescoTransactionSupport.bindResource(HIBERNATE_SESSION_EVENT_LISTENER, listener);
        }
        return listener;
    }

   

    
}