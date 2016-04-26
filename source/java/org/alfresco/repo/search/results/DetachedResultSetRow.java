package org.alfresco.repo.search.results;

import java.util.Map;

import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;

/**
 * Detatched result set row
 * @author andyh
 *
 */
public class DetachedResultSetRow extends AbstractResultSetRow
{
    private ChildAssociationRef car;
    
    /**
     * Detached result set row
     * @param resultSet ResultSet
     * @param row ResultSetRow
     */
    public DetachedResultSetRow(ResultSet resultSet, ResultSetRow row)
    {
        super(resultSet, row.getIndex());
        car = row.getChildAssocRef();
        properties = row.getValues();
    }

    public QName getQName()
    {
        return car.getQName();
    }

    public NodeRef getNodeRef()
    {
        return car.getChildRef();
    }

    public ChildAssociationRef getChildAssocRef()
    {
        return car;
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
