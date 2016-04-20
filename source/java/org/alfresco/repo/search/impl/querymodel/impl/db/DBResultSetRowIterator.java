package org.alfresco.repo.search.impl.querymodel.impl.db;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * @author Andy
 *
 */
public class DBResultSetRowIterator extends AbstractResultSetRowIterator
{

    /**
     * @param resultSet ResultSet
     */
    public DBResultSetRowIterator(ResultSet resultSet)
    {
        super(resultSet);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AbstractResultSetRowIterator#next()
     */
    @Override
    public ResultSetRow next()
    {
        return new DBResultSetRow((DBResultSet)getResultSet(), moveToNextPosition());
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.AbstractResultSetRowIterator#previous()
     */
    @Override
    public ResultSetRow previous()
    {
        return new DBResultSetRow((DBResultSet)getResultSet(), moveToPreviousPosition());
    }

}
