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
