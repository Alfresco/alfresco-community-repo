/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.repo.site;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.query.CannedQuerySortDetails;
import org.alfresco.query.CannedQuerySortDetails.SortOrder;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * A canned query for fetching site containers.
 * 
 * @author steveglover
 *
 */
public class SiteContainersCannedQuery extends AbstractCannedQueryPermissions<FileInfo>
{
	private FileFolderService fileFolderService;
	private NodeService nodeService;

    public SiteContainersCannedQuery(FileFolderService fileFolderService, NodeService nodeService, CannedQueryParameters parameters, MethodSecurityBean<FileInfo> methodSecurity)
    {
        super(parameters, methodSecurity);
        this.fileFolderService = fileFolderService;
        this.nodeService = nodeService;
    }
    
    @Override
    protected List<FileInfo> queryAndFilter(CannedQueryParameters parameters)
    {
        SiteContainersCannedQueryParams paramBean = (SiteContainersCannedQueryParams)parameters.getParameterBean();
        
        NodeRef siteNodeRef = paramBean.getSiteNodeRef();

        // need to get all folders to check for site container aspect since the FileFolderService won't allow us to filter
        // on aspects. Number of site containers should be relatively small.
    	final List<FileInfo> containers = new ArrayList<FileInfo>(10);

    	int skip = 0;
    	int maxItems = 50;
        List<Pair<QName, Boolean>> sortProps = null;
        PagingRequest pagingRequest = new PagingRequest(skip, maxItems);
        PagingResults<FileInfo> pagingResults = null;
        do
        {
	        pagingResults = fileFolderService.list(siteNodeRef, false, true, null, sortProps, pagingRequest);

	        for(FileInfo folder : pagingResults.getPage())
	        {
	            NodeRef containerNodeRef = folder.getNodeRef();
	            if(nodeService.hasAspect(containerNodeRef, SiteModel.ASPECT_SITE_CONTAINER))
	            {
	            	containers.add(folder);
	            }
	        }

	        if(pagingResults.hasMoreItems())
	        {
		        skip += maxItems;
		        pagingRequest = new PagingRequest(skip, maxItems);
	        }
        } while(pagingResults.hasMoreItems());

        return containers;
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        return true;
    }

    @SuppressWarnings({ "unchecked"})
    protected List<FileInfo> applyPostQuerySorting(List<FileInfo> results, CannedQuerySortDetails sortDetails)
    {
        @SuppressWarnings("rawtypes")
        final List<Pair<Object, SortOrder>> sortPairs = (List)sortDetails.getSortPairs();
        if (sortPairs.size() > 0)
        {
            Collections.sort(results, new FileInfoComparator(sortPairs));
        }

        return results;
    }
    
    private static class FileInfoComparator implements Comparator<FileInfo>
    {
    	private List<Pair<Object, SortOrder>> sortPairs;
    	
		public FileInfoComparator(List<Pair<Object, SortOrder>> sortPairs)
		{
			super();
			this.sortPairs = sortPairs;
		}

		@Override
		public int compare(FileInfo o1, FileInfo o2)
		{
			int ret = 0;

			for(Pair<? extends Object, SortOrder> pair : sortPairs)
			{
				if(pair.getFirst().equals(SiteContainersCannedQueryParams.SortFields.ContainerName))
				{
					ret = o1.getName().compareTo(o2.getName());
					if(pair.getSecond().equals(SortOrder.DESCENDING))
					{
						ret = ret * -1;
					}
				}
			}

			return ret;
		}
    }
}
