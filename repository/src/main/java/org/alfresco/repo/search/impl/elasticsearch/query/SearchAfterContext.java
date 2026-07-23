/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.elasticsearch.query;

import java.util.List;

public final class SearchAfterContext
{
    private final String pitId;
    private final String keepAlive;
    private final List<String> searchAfter;

    public SearchAfterContext(String pitId, String keepAlive, List<String> searchAfter)
    {
        this.pitId = pitId;
        this.keepAlive = keepAlive;
        this.searchAfter = searchAfter == null ? List.of() : searchAfter;
    }

    public String getPitId()
    {
        return pitId;
    }

    public String getKeepAlive()
    {
        return keepAlive;
    }

    // the previous page's last sort values, or an empty list for the first page
    public List<String> getSearchAfter()
    {
        return searchAfter;
    }
}
