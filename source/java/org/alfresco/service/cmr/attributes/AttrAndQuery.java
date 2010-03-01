/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.service.cmr.attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * And aggregate query.
 * @author britt
 */
public class AttrAndQuery extends AttrQuery
{
    private static final long serialVersionUID = 5452165715643751502L;

    private List<AttrQuery> fSubQueries;
    
    public AttrAndQuery(List<AttrQuery> queries)
    {
        fSubQueries = queries;
    }
    
    public AttrAndQuery(AttrQuery... queries)
    {
        fSubQueries = new ArrayList<AttrQuery>();
        for (AttrQuery query : queries)
        {
            fSubQueries.add(query);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttrQuery#getPredicate(org.alfresco.service.cmr.attributes.AttrQueryHelper)
     */
    @Override
    public String getPredicate(AttrQueryHelper helper)
    {
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        for (int i = 0; i < fSubQueries.size() - 1; i++)
        {
            builder.append(fSubQueries.get(i).getPredicate(helper));
            builder.append(" and ");
        }
        builder.append(fSubQueries.get(fSubQueries.size() - 1).getPredicate(helper));
        builder.append(')');
        return builder.toString();
    }
}
