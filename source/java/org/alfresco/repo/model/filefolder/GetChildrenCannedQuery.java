/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.model.filefolder;

import java.util.List;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodePropertyHelper;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.model.filefolder.HiddenAspect.Visibility;
import org.alfresco.repo.node.getchildren.FilterProp;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;

/**
 * GetChidren canned query for files and folders.
 * 
 * To get paged list of files and folders of a parent folder filtered by child type.
 * Also optionally filtered and/or sorted by one or more properties (up to three).
 * 
 * This is the same as the nodes getchildren canned query, except it takes into account hidden files and folders.
 * 
 * @since 4.1.1
 * @author steveglover
 *
 */
public class GetChildrenCannedQuery extends org.alfresco.repo.node.getchildren.GetChildrenCannedQuery
{
    private HiddenAspect hiddenAspect;

    public GetChildrenCannedQuery(
            NodeDAO nodeDAO,
            QNameDAO qnameDAO,
            CannedQueryDAO cannedQueryDAO,
            NodePropertyHelper nodePropertyHelper,
            TenantService tenantService,
            MethodSecurityBean<NodeRef> methodSecurity,
            CannedQueryParameters params,
            HiddenAspect hiddenAspect)
    {
    	super(nodeDAO, qnameDAO, cannedQueryDAO, nodePropertyHelper, tenantService, methodSecurity, params);
    	this.hiddenAspect = hiddenAspect;
    }

    @Override
    protected UnsortedChildQueryCallback getUnsortedChildQueryCallback(final List<NodeRef> rawResult, final int requestedCount)
    {
        UnsortedChildQueryCallback callback = new FileFolderUnsortedChildQueryCallback(rawResult, requestedCount);
        return callback;
    }
    
    @Override
    protected FilterSortChildQueryCallback getFilterSortChildQuery(final List<FilterSortNode> children, final List<FilterProp> filterProps)
    {
    	FilterSortChildQueryCallback callback = new FileFolderFilterSortChildQueryCallback(children, filterProps);
    	return callback;
    }

    private class FileFolderFilterSortChildQueryCallback extends DefaultFilterSortChildQueryCallback
    {
		public FileFolderFilterSortChildQueryCallback(List<FilterSortNode> children, List<FilterProp> filterProps)
		{
			super(children, filterProps);
		}

		@Override
		protected boolean include(FilterSortNode node)
		{
			boolean ret = super.include(node);

            // only visible files are returned, relative to the client type.
			try
			{
	            final Client client = FileFilterMode.getClient();
	        	return ret && hiddenAspect.getVisibility(client, node.getNodeRef()) != Visibility.NotVisible;
            }
            catch(AccessDeniedException e)
            {
            	// user may not have permission to determine the visibility of the node
            	return ret;
            }
		}
    	
    }

    private class FileFolderUnsortedChildQueryCallback extends DefaultUnsortedChildQueryCallback
    {
		public FileFolderUnsortedChildQueryCallback(List<NodeRef> rawResult,int requestedCount)
		{
			super(rawResult, requestedCount);
		}

		@Override
		protected boolean include(NodeRef nodeRef)
		{
			boolean ret = super.include(nodeRef);

            // only visible files are returned, relative to the client type.
			try
			{
	            final Client client = FileFilterMode.getClient();
	        	return ret && hiddenAspect.getVisibility(client, nodeRef) != Visibility.NotVisible;
            }
            catch(AccessDeniedException e)
            {
            	// user may not have permission to determine the visibility of the node
            	return ret;
            }
		}
    	
    }

}
