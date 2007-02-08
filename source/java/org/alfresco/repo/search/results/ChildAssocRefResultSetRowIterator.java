/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search.results;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

public class ChildAssocRefResultSetRowIterator extends AbstractResultSetRowIterator
{

    public ChildAssocRefResultSetRowIterator(ResultSet resultSet)
    {
        super(resultSet);
    }

    @Override
    public ResultSetRow next()
    {
       return new ChildAssocRefResultSetRow((ChildAssocRefResultSet)getResultSet(), moveToNextPosition());
    }

    @Override
    public ResultSetRow previous()
    {
        return new ChildAssocRefResultSetRow((ChildAssocRefResultSet)getResultSet(), moveToPreviousPosition());
    }

}
