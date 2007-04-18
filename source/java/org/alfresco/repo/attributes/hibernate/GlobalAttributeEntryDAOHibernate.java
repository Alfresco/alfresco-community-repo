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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes.hibernate;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.repo.attributes.GlobalAttributeEntry;
import org.alfresco.repo.attributes.GlobalAttributeEntryDAO;
import org.alfresco.repo.attributes.GlobalAttributeEntryImpl;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of Global Attribute Entries.
 * @author britt
 */
public class GlobalAttributeEntryDAOHibernate extends HibernateDaoSupport
        implements GlobalAttributeEntryDAO
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#delete(org.alfresco.repo.attributes.GlobalAttributeEntry)
     */
    public void delete(GlobalAttributeEntry entry)
    {
        getSession().delete(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#delete(java.lang.String)
     */
    public void delete(String name)
    {
        delete((GlobalAttributeEntry)getSession().get(GlobalAttributeEntryImpl.class, name));
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#get(java.lang.String)
     */
    public GlobalAttributeEntry get(String name)
    {
        return (GlobalAttributeEntry)getSession().get(GlobalAttributeEntryImpl.class, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.GlobalAttributeEntryDAO#save(org.alfresco.repo.attributes.GlobalAttributeEntry)
     */
    public void save(GlobalAttributeEntry entry)
    {
        getSession().save(entry);
    }
}
