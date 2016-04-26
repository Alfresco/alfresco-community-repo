package org.alfresco.repo.search.results;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 *
 */
public class SortedResultSetRow extends AbstractResultSetRow implements ResultSetRow
{

    /**
     * @param resultSet SortedResultSet
     * @param index int
     */
    public SortedResultSetRow(SortedResultSet resultSet, int index)
    {
        super(resultSet, index);
    }

    public NodeRef getNodeRef(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    public float getScore(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }

    protected Map<QName, Serializable> getDirectProperties()
    {
        SortedResultSet srs = (SortedResultSet) getResultSet();
        return srs.getNodeService().getProperties(srs.getNodeRef(getIndex()));
    }
  

    

}
