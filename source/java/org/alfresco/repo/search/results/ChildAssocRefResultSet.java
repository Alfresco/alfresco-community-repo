/*
 * Created on 07-Jun-2005
 *
 * TODO Comment this class
 * 
 * 
 */
package org.alfresco.repo.search.results;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;

/**
 * Child assoc result set
 * @author andyh
 *
 */
public class ChildAssocRefResultSet extends AbstractResultSet
{
    private List<ChildAssociationRef> cars;
    NodeService nodeService;
    
    /**
     * Normal constructor
     * @param nodeService NodeService
     * @param cars List<ChildAssociationRef>
     */
    public ChildAssocRefResultSet(NodeService nodeService, List<ChildAssociationRef> cars)
    {
        super();
        this.nodeService = nodeService;
        this.cars = cars;
    }
    
    /**
     * Constructor that may expand all child assoc parents provided
     * @param nodeService NodeService
     * @param nodeRefs List<NodeRef> nodeRefs
     * @param resolveAllParents boolean
     */
    public ChildAssocRefResultSet(NodeService nodeService, List<NodeRef> nodeRefs, boolean resolveAllParents)
    {
        super();
        this.nodeService = nodeService;
        List<ChildAssociationRef> cars = new ArrayList<ChildAssociationRef>(nodeRefs.size());
        for(NodeRef nodeRef : nodeRefs)
        {
            if(resolveAllParents)
            {
                cars.addAll(nodeService.getParentAssocs(nodeRef));
            }
            else
            {
                cars.add(nodeService.getPrimaryParent(nodeRef));
            }
        }
        this.cars = cars;
    }

    public int length()
    {
        return cars.size();
    }

    public NodeRef getNodeRef(int n)
    {
        return cars.get(n).getChildRef();
    }
    
    public ChildAssociationRef getChildAssocRef(int n)
    {
        return cars.get(n);
    }

    public ResultSetRow getRow(int i)
    {
        return new ChildAssocRefResultSetRow(this, i);
    }

    public Iterator<ResultSetRow> iterator()
    {
        return new ChildAssocRefResultSetRowIterator(this);
    }
    
    
    /* package */ NodeService getNodeService()
    {
        return nodeService;
    }

    public ResultSetMetaData getResultSetMetaData()
    {
        return new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, new SearchParameters());
    }

    public int getStart()
    {
       throw new UnsupportedOperationException();
    }

    public boolean hasMore()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNumberFound()
     */
    @Override
    public long getNumberFound()
    {
        return cars.size();
    }
}
