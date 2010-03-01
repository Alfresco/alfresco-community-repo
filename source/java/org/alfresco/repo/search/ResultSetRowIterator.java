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
package org.alfresco.repo.search;

import java.util.ListIterator;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * A typed ListIterator over Collections containing ResultSetRow elements
 * 
 * @author andyh
 */
public interface ResultSetRowIterator extends ListIterator<ResultSetRow>
{
    /**
     * Get the underlying result set
     * 
     * @return - the result set
     */
    public ResultSet getResultSet();

    /**
     * Does the result set allow reversal?
     * 
     * @return - true if the result set can be navigated in reverse.
     */
    public boolean allowsReverse();
}
