/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.copy.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.CopyService.CopyInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Factory producing queries for the {@link CopyService}
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class GetCopiesCannedQueryFactory extends AbstractCopyCannedQueryFactory<CopyInfo>
{
    @Override
    public CannedQuery<CopyInfo> getCannedQuery(CannedQueryParameters parameters)
    {
        return new GetCopiesCannedQuery(parameters, methodSecurity);
    }
    
    /**
     * Query to find nodes copied <i>from</i> a given node, optionally filtering out
     * based on specific values.
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    private class GetCopiesCannedQuery extends AbstractCannedQueryPermissions<CopyInfo>
    {
        private GetCopiesCannedQuery(CannedQueryParameters parameters, MethodSecurityBean<CopyInfo> methodSecurity)
        {
            super(parameters, methodSecurity);
        }

        @Override
        protected List<CopyInfo> queryAndFilter(CannedQueryParameters parameters)
        {
            CopyCannedQueryDetail detail = GetCopiesCannedQueryFactory.this.getDetail(parameters);
            // Build parameters
            CopyParametersEntity queryParameters = new CopyParametersEntity();
            // Original node
            Pair<Long, NodeRef> originalNodePair = nodeDAO.getNodePair(detail.originalNodeRef);
            if (originalNodePair == null)
            {
                return Collections.emptyList();         // Shortcut
            }
            queryParameters.setOriginalNodeId(originalNodePair.getFirst());
            // cm:original association type ID
            Pair<Long, QName> assocTypeQNamePair = qnameDAO.getQName(ContentModel.ASSOC_ORIGINAL);
            if (assocTypeQNamePair == null)
            {
                return Collections.emptyList();         // Shortcut
            }
            queryParameters.setOriginalAssocTypeId(assocTypeQNamePair.getFirst());
            // cm:name property ID
            Pair<Long, QName> propQNamePair = qnameDAO.getQName(ContentModel.PROP_NAME);
            if (propQNamePair == null)
            {
                return Collections.emptyList();         // Shortcut
            }
            queryParameters.setNamePropId(propQNamePair.getFirst());
            // Copied parent node
            if (detail.copyParentNodeRef != null)
            {
                Pair<Long, NodeRef> copyParentNodePair = nodeDAO.getNodePair(detail.copyParentNodeRef);
                if (copyParentNodePair == null)
                {
                    return Collections.emptyList();         // Shortcut
                }
                queryParameters.setCopyParentNodeId(copyParentNodePair.getFirst());
            }
            // Now query
            int resultsRequired = parameters.getResultsRequired();
            List<CopyEntity> copies = cannedQueryDAO.executeQuery(
                    "alfresco.query.copy", "select_GetCopies",
                    queryParameters,
                    0, resultsRequired);
            // Convert them
            List<CopyInfo> results = new ArrayList<CopyService.CopyInfo>(copies.size());
            for (CopyEntity copy : copies)
            {
                CopyInfo result = new CopyInfo(copy.getCopy().getNodeRef(), copy.getCopyName());
                results.add(result);
            }
            // Done
            return results;
        }
    }
}