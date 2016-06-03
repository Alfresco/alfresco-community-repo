package org.alfresco.repo.node.getchildren;

import org.alfresco.repo.query.NodeWithTargetsEntity;

/**
 * Parameter objects for {@link GetChildrenWithTargetAssocsAuditableCannedQuery}.
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class GetChildrenWithTargetAssocsAuditableCannedQueryParams extends NodeWithTargetsEntity
{
    public GetChildrenWithTargetAssocsAuditableCannedQueryParams(Long parentNodeId,
                                         Long nameQNameId,
                                         Long contentTypeQNameId,
                                         Long assocTypeId)
                                         
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId, assocTypeId);
    }
}
