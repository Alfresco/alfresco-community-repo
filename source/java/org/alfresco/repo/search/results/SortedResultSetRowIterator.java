package org.alfresco.repo.search.results;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * @author andyh
 *
 */
public class SortedResultSetRowIterator extends AbstractResultSetRowIterator
{

    /**
     * @param resultSet ResultSet
     */
    public SortedResultSetRowIterator(ResultSet resultSet)
    {
        super(resultSet);
    }

    @Override
    public ResultSetRow next()
    {
       return new SortedResultSetRow((SortedResultSet)getResultSet(), moveToNextPosition());
    }

    @Override
    public ResultSetRow previous()
    {
        return new SortedResultSetRow((SortedResultSet)getResultSet(), moveToPreviousPosition());
    }

}
