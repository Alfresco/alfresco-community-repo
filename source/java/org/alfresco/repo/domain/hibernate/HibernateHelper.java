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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.domain.hibernate;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

/**
 * Helper methods related to Hibernate
 * 
 * @author Derek Hulley
 */
public class HibernateHelper
{
    /**
     * Helper method to scroll through the results of a query and delete all the
     * resulting access control entries, performing batch flushes.
     * 
     * @param session the session to use for the deletions
     * @param query the query with all parameters set and that will return
     *      {@link org.alfresco.repo.domain.DbAccessControlEntry access control entry} instances
     * @return Returns the number of entries deleted
     */
    public static int deleteDbAccessControlEntries(Session session, Query query)
    {
        ScrollableResults entities = query.scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;
        while (entities.next())
        {
            DbAccessControlEntry entry = (DbAccessControlEntry) entities.get(0);
            entry.delete();
            if (++count % 50 == 0)
            {
                session.flush();
                session.clear();
            }
        }
        return count;
    }
}
