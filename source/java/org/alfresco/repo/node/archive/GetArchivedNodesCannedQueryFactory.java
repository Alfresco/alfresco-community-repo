/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.node.archive;

import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * Canned query factory for getting archived nodes.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 4.2
 */
public class GetArchivedNodesCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<ArchivedNodeEntity>
{
    private AuthorityService authorityService;
    protected NodeService nodeService;

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    public CannedQuery<ArchivedNodeEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        return (CannedQuery<ArchivedNodeEntity>) new GetArchivedNodesCannedQuery(cannedQueryDAO,
                    nodeDAO, methodSecurity, parameters);
    }

    /**
     * @param archiveStoreRootNodeRef
     * @param filter
     * @param filterIgnoreCase
     * @param pagingRequest
     * @param sortOrderAscending
     * @return an implementation that will execute the query
     */
    public CannedQuery<ArchivedNodeEntity> getCannedQuery(NodeRef archiveStoreRootNodeRef,
                String filter, boolean filterIgnoreCase, PagingRequest pagingRequest,
                boolean sortOrderAscending)
    {
        ParameterCheck.mandatory("archiveStoreRootNodeRef", archiveStoreRootNodeRef);
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        int requestTotalCountMax = pagingRequest.getRequestTotalCountMax();

        Pair<Long, Long> nodeIdAssocTypeIdPair = getNodeIdAssocTypeIdPair(archiveStoreRootNodeRef);
        GetArchivedNodesCannedQueryParams paramBean = new GetArchivedNodesCannedQueryParams(
                    nodeIdAssocTypeIdPair.getFirst(), nodeIdAssocTypeIdPair.getSecond(), filter,
                    filterIgnoreCase, getQNameId(ContentModel.PROP_NAME), sortOrderAscending);

        // page details
        CannedQueryPageDetails cqpd = new CannedQueryPageDetails(pagingRequest.getSkipCount(),
                    pagingRequest.getMaxItems(), CannedQueryPageDetails.DEFAULT_PAGE_NUMBER,
                    CannedQueryPageDetails.DEFAULT_PAGE_COUNT);

        // create query params holder
        CannedQueryParameters params = new CannedQueryParameters(paramBean, cqpd, null,
                    requestTotalCountMax, pagingRequest.getQueryExecutionId());

        // return canned query instance
        return getCannedQuery(params);
    }

    private Pair<Long, Long> getNodeIdAssocTypeIdPair(NodeRef archiveStoreRootNodeRef)
    {
        String userID = AuthenticationUtil.getFullyAuthenticatedUser();
        if (userID == null)
        {
            throw new AuthenticationException("Failed to authenticate. Current user, ", new Object[] { userID });
        }
        
        if (archiveStoreRootNodeRef == null || !nodeService.exists(archiveStoreRootNodeRef))
        {
            throw new InvalidNodeRefException("Invalid archive store root node Ref.",
                        archiveStoreRootNodeRef);
        }

        if (authorityService.isAdminAuthority(userID))
        {
            return new Pair<Long, Long>(getNodeId(archiveStoreRootNodeRef),
                        getQNameId(ContentModel.ASSOC_CHILDREN));
        }
        else
        {
            List<ChildAssociationRef> list = nodeService.getChildrenByName(archiveStoreRootNodeRef,
                        ContentModel.ASSOC_ARCHIVE_USER_LINK, Collections.singletonList(userID));

            // Empty list means that the current user hasn't deleted anything yet.
            if (list.isEmpty())
            {
                return new Pair<Long, Long>(-1L, -1L);
            }
            NodeRef userArchive = list.get(0).getChildRef();
            return new Pair<Long, Long>(getNodeId(userArchive),
                        getQNameId(ContentModel.ASSOC_ARCHIVED_LINK));
        }
    }
}
