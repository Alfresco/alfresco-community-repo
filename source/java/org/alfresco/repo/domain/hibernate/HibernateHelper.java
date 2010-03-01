/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
        ScrollableResults entities = null;
        int count = 0;
        try
        {
            entities = query.scroll(ScrollMode.FORWARD_ONLY);
        
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
        }
        finally
        {
            if(entities != null)
            {
                entities.close();
            }
        }
        
        return count;
    }
}
