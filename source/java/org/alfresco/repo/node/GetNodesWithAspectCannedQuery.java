package org.alfresco.repo.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GetNodesWithAspect canned query
 * 
 * To get paged list of nodes with the specified aspects.
 *
 * @author Nick Burch
 * @since 4.1
 */
public class GetNodesWithAspectCannedQuery extends AbstractCannedQueryPermissions<NodeRef>
{
    private Log logger = LogFactory.getLog(getClass());
    
    private NodeDAO nodeDAO;
    private TenantService tenantService;
    
    public GetNodesWithAspectCannedQuery(
            NodeDAO nodeDAO,
            TenantService tenantService,
            MethodSecurityBean<NodeRef> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        
        this.nodeDAO = nodeDAO;
        this.tenantService = tenantService;
    }
    
    @Override
    protected List<NodeRef> queryAndFilter(CannedQueryParameters parameters)
    {
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        // Get parameters
        GetNodesWithAspectCannedQueryParams paramBean = (GetNodesWithAspectCannedQueryParams)parameters.getParameterBean();
        
        // Get store, if requested
        final StoreRef storeRef = paramBean.getStoreRef();
        
        // Note - doesn't currently support sorting 
       
        // Get filter details
        final Set<QName> aspectQNames = paramBean.getAspectQNames();

        
        // Find all the available nodes
        // Doesn't limit them here, as permissions will be applied post-query
        // TODO Improve this to permission check and page in-line, so we
        //  can stop the query earlier if possible
        final List<NodeRef> result = new ArrayList<NodeRef>(100);
        
        nodeDAO.getNodesWithAspects(
                aspectQNames, Long.MIN_VALUE, Long.MAX_VALUE,
                new NodeRefQueryCallback() {
                    @Override
                    public boolean handle(Pair<Long, NodeRef> nodePair)
                    {
                        NodeRef nodeRef = nodePair.getSecond();
                        if (storeRef == null || nodeRef.getStoreRef().equals(storeRef))
                        {
                            result.add(nodeRef);
                        }
                        
                        // Always ask for the next one
                        return true;
                    }
                }
        );
        
        if (start != null)
        {
            logger.debug("Base query: "+result.size()+" in "+(System.currentTimeMillis()-start)+" msecs");
        }
        
        return result;
    }
}
