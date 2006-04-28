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

import org.hibernate.CacheMode;
import org.hibernate.ObjectDeletedException;
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
     * results, performing batch flushes.  This will handle large resultsets by
     * pulling the results directly in from the query.  For certain circumstances, it
     * may be better to perform a bulk delete directly instead.
     * 
     * @param session the session to use for the deletions
     * @param query the query with all parameters set
     * @return Returns the number of entities deleted, regardless of type
     */
    public static int deleteQueryResults(Session session, Query query)
    {
        ScrollableResults entities = query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);
        int count = 0;
        while (entities.next())
        {
            Object[] entityResults = entities.get();
            for (Object object : entityResults)
            {
                try
                {
                    session.delete(object);
                }
                catch (ObjectDeletedException e)
                {
                    // ignore - it's what we wanted
                }
                if (++count % 50 == 0)
                {
                    session.flush();
                    session.clear();
                }
            }
        }
        return count;
    }
}
