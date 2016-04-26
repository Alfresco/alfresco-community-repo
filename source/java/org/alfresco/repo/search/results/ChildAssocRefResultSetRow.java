package org.alfresco.repo.search.results;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Row in child assoc ref result set.
 * @author andyh
 *
 */
public class ChildAssocRefResultSetRow extends AbstractResultSetRow
{
    /**
     * Row in child assoc ref result set
     * @param resultSet ChildAssocRefResultSet
     * @param index int
     */
    public ChildAssocRefResultSetRow(ChildAssocRefResultSet resultSet, int index)
    {
        super(resultSet, index);
    }

    public QName getQName()
    {
        return ((ChildAssocRefResultSet)getResultSet()).getChildAssocRef(getIndex()).getQName();
    }

    @Override
    protected Map<QName, Serializable> getDirectProperties()
    {
        return ((ChildAssocRefResultSet)getResultSet()).getNodeService().getProperties(getNodeRef());
    }

    public ChildAssociationRef getChildAssocRef()
    {
        return ((ChildAssocRefResultSet)getResultSet()).getChildAssocRef(getIndex());
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

}
