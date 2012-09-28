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
package org.alfresco.repo.download.cannedquery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.security.permissions.impl.acegi.AbstractCannedQueryPermissions;
import org.alfresco.repo.security.permissions.impl.acegi.MethodSecurityBean;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;

/**
 * This class provides the GetDownloads canned queries} used by the
 * {@link DownloadService}.deleteDOwnloads.
 * 
 * @author Alex Miller
 */
public class GetDownloadsCannedQuery extends AbstractCannedQueryPermissions<DownloadEntity>
{
    private static final String QUERY_NAMESPACE = "alfresco.query.downloads";
    private static final String QUERY_SELECT_GET_DOWNLOADS = "select_GetDownloadsBeforeQuery";
    
    private final CannedQueryDAO cannedQueryDAO;
    
    public GetDownloadsCannedQuery(
            CannedQueryDAO cannedQueryDAO,
            MethodSecurityBean<DownloadEntity> methodSecurity,
            CannedQueryParameters params)
    {
        super(params, methodSecurity);
        this.cannedQueryDAO = cannedQueryDAO;
    }
    
    @Override
    protected List<DownloadEntity> queryAndFilter(CannedQueryParameters parameters)
    {
        Object paramBeanObj = parameters.getParameterBean();
        if (paramBeanObj == null)
        {
            throw new NullPointerException("Null GetDownloadss query params");
        }
        
        GetDownloadsCannedQueryParams paramsBean = (GetDownloadsCannedQueryParams)paramBeanObj;

        // note: refer to SQL for specific DB filtering (eg.parent node and optionally blog integration aspect, etc)
        List<DownloadEntity> results = cannedQueryDAO.executeQuery(QUERY_NAMESPACE, QUERY_SELECT_GET_DOWNLOADS, paramBeanObj, 0, Integer.MAX_VALUE);

        List<DownloadEntity> filteredResults = new ArrayList<DownloadEntity>();
        for (DownloadEntity entity : results) 
        {
            Date createdDate = DefaultTypeConverter.INSTANCE.convert(Date.class, entity.getCreatedDate());
            Date modifiedDate = DefaultTypeConverter.INSTANCE.convert(Date.class, entity.getModifiedDate());
            
            if (modifiedDate == null)
            {
                modifiedDate = createdDate;
            }
            if (modifiedDate.before(paramsBean.getBefore()))
            {
                filteredResults.add(entity);
            }
            else
            {
                break;
            }
        }
        return filteredResults;
    }
    
    @Override
    protected boolean isApplyPostQuerySorting()
    {
        // No post-query sorting. It's done within the queryAndFilter() method above.
        return false;
    }
}