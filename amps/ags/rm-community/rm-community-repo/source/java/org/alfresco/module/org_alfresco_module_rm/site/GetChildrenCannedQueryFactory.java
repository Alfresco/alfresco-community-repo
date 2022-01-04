/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

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
