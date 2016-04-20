package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * @author Andy
 *
 */
public class SolrJSONResultSetRowIterator extends AbstractResultSetRowIterator
{

    /**
     * @param resultSet ResultSet
     */
    public SolrJSONResultSetRowIterator(ResultSet resultSet)
    {
        super(resultSet);
        // TODO Auto-generated constructor stub
    }

    public ResultSetRow next()
    {
        return new SolrJSONResultSetRow((SolrJSONResultSet)getResultSet(), moveToNextPosition());
    }

    public ResultSetRow previous()
    {
        return new SolrJSONResultSetRow((SolrJSONResultSet)getResultSet(), moveToPreviousPosition());
    }
}
