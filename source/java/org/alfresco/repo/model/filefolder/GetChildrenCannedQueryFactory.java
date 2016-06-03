package org.alfresco.repo.model.filefolder;

import java.util.List;
import java.util.Set;

import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * GetChidren canned query factory for files and folders. 
 * 
 * @since 4.1.1
 * @author steveglover, janv
 *
 */
public class GetChildrenCannedQueryFactory extends org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory
{
    private HiddenAspect hiddenAspect;
    private Set<QName> ignoreAspectQNames;
    
    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }
    
    public CannedQuery<NodeRef> getCannedQuery(NodeRef parentRef, String pattern, Set<QName> assocTypeQNames, Set<QName> childTypeQNames, Set<QName> ignoreAspectQNames, List<FilterProp> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
    {
        this.ignoreAspectQNames = ignoreAspectQNames;
        
        return super.getCannedQuery(parentRef, pattern, assocTypeQNames, childTypeQNames, filterProps, sortProps, pagingRequest);
    }
    
    @Override
    public CannedQuery<NodeRef> getCannedQuery(CannedQueryParameters parameters)
    {
        NodePropertyHelper nodePropertyHelper = new NodePropertyHelper(dictionaryService, qnameDAO, localeDAO, contentDataDAO);
        
        return (CannedQuery<NodeRef>) new GetChildrenCannedQuery(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, nodeService, methodSecurity, parameters, hiddenAspect, dictionaryService, ignoreAspectQNames);
    }
}
