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

import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;

/**
 * @author andyh
 *
 */
public class BaseOrdering implements Ordering
{
    private Column column;
    
    private Order order;
    
    public BaseOrdering(Column column, Order order)
    {
        this.column = column;
        this.order = order;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Ordering#getColumn()
     */
    public Column getColumn()
    {
       return column;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.Ordering#getOrder()
     */
    public Order getOrder()
    {
       return order;
    }
    
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BaseOrdering[");
        builder.append("Column=" + getColumn()).append(", ");
        builder.append("Order=" + getOrder());
        builder.append("]");
        return builder.toString();
    }

}
