package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;

/**
 * @author Andy
 *
 */
public class DBResultSetRow extends AbstractResultSetRow
{

    /**
     * @param resultSet ResultSet
     * @param index int
     */
    public DBResultSetRow(ResultSet resultSet, int index)
    {
        super(resultSet, index);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRefs()
     */
    @Override
    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRef(java.lang.String)
     */
    @Override
    public NodeRef getNodeRef(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScores()
     */
    @Override
    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScore(java.lang.String)
     */
    @Override
    public float getScore(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

  
}
