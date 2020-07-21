/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.event2.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Implementation of the user filter.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EventUserFilter implements EventFilter<String>
{
    private Set<String> filteredUsers;
    private boolean userNamesAreCaseSensitive;

    public EventUserFilter(String filteredUsersStr, boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
        this.filteredUsers = parseFilterList(filteredUsersStr);
    }

    private Set<String> parseFilterList(String str)
    {
        Set<String> set = new HashSet<>();

        StringTokenizer st = new StringTokenizer(str, ",");
        while (st.hasMoreTokens())
        {
            String entry = st.nextToken().trim();
            if (!entry.isEmpty())
            {
                set.add((userNamesAreCaseSensitive) ? entry : entry.toLowerCase());
            }
        }
        return set;
    }

    @Override
    public boolean isExcluded(String user)
    {
        if (user == null)
        {
            user = "null";
        }
        return filteredUsers.contains((userNamesAreCaseSensitive) ? user : user.toLowerCase());
    }
}