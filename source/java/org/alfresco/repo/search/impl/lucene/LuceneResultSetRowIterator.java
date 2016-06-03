package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Iterate over the rows in a LuceneResultSet
 * 
 * @author andyh
 * 
 */
public class LuceneResultSetRowIterator extends AbstractResultSetRowIterator
{
    /**
     * Create an iterator over the result set. Follows standard ListIterator
     * conventions
     * 
     * @param resultSet LuceneResultSet
     */
    public LuceneResultSetRowIterator(LuceneResultSet resultSet)
    {
        super(resultSet);
    }

    public ResultSetRow next()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToNextPosition());
    }

    public ResultSetRow previous()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToPreviousPosition());
    }
}
