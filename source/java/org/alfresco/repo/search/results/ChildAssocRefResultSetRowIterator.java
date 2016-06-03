package org.alfresco.repo.search.results;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Iterate over child asooc refs
 * @author andyh
 *
 */
public class ChildAssocRefResultSetRowIterator extends AbstractResultSetRowIterator
{

    /**
     * Source result set
     * @param resultSet ResultSet
     */
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
