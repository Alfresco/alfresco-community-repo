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
package org.alfresco.repo.search.impl.querymodel.impl;

import java.util.List;

import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.Query;
import org.alfresco.repo.search.impl.querymodel.Source;

/**
 * @author andyh
 */
public class BaseQuery implements Query
{
    private Source source;

    private List<Column> columns;

    private Constraint constraint;

    private List<Ordering> orderings;

    public BaseQuery(List<Column> columns, Source source, Constraint constraint, List<Ordering> orderings)
    {
        this.columns = columns;
        this.source = source;
        this.constraint = constraint;
        this.orderings = orderings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Query#getColumns()
     */
    public List<Column> getColumns()
    {
        return columns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Query#getConstraint()
     */
    public Constraint getConstraint()
    {
        return constraint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Query#getOrderings()
     */
    public List<Ordering> getOrderings()
    {
        return orderings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.Query#getSource()
     */
    public Source getSource()
    {
        return source;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseQuery[\n");
        builder.append("\tcolumns=").append(getColumns()).append("\n");
        builder.append("\tsource=").append(getSource()).append("\n");
        builder.append("\tconstraint=").append(getConstraint()).append("\n");
        builder.append("\torderings=").append(getOrderings()).append("\n");
        builder.append("]");
        return builder.toString();
    }

}
