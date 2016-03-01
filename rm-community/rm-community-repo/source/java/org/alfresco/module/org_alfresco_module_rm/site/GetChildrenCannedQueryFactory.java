 
package org.alfresco.module.org_alfresco_module_rm.site;

import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Override default implementation to add rma:rmsite to list of returned site types.
 * 
 * See https://issues.alfresco.com/jira/browse/RM-387
 * 
 * @author Roy Wetherall
 */
public class GetChildrenCannedQueryFactory extends org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory
                                           implements RecordsManagementModel
{
    @Override
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, String pattern, Set<QName> assocTypeQNames, Set<QName> childTypeQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        childTypeQNames.add(TYPE_RM_SITE);
        return super.getCannedQuery(parentRef, pattern, assocTypeQNames, childTypeQNames, filterProps, sortProps, pagingRequest);
    }
}
