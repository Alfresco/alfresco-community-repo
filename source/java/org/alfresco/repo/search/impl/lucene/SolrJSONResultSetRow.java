package org.alfresco.repo.search.impl.lucene;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 */
public class SolrJSONResultSetRow extends AbstractResultSetRow
{

    /**
     * @param resultSet ResultSet
     * @param index int
     */
    public SolrJSONResultSetRow(ResultSet resultSet, int index)
    {
        super(resultSet, index);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRef(java.lang.String)
     */
    @Override
    public NodeRef getNodeRef(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getNodeRefs()
     */
    @Override
    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScore(java.lang.String)
     */
    @Override
    public float getScore(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetRow#getScores()
     */
    @Override
    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }
    
    protected Map<QName, Serializable> getDirectProperties()
    {
        SolrJSONResultSet rs = (SolrJSONResultSet) getResultSet();
        return rs.getNodeService().getProperties(rs.getNodeRef(getIndex()));
    }

}
