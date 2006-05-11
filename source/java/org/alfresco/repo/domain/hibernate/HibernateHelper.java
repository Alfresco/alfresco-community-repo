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
