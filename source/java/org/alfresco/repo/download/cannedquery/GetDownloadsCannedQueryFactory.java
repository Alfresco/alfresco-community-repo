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

import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.query.CannedQuery;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryParameters;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.query.AbstractQNameAwareCannedQueryFactory;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * A {@link CannedQueryFactory} for queries relating to {@link DownloadEntity download entities}.
 * 
 * @author Alex Miller
 * 
 * @see DownloadService#deleteDownloads(Date)
 */
public class GetDownloadsCannedQueryFactory extends AbstractQNameAwareCannedQueryFactory<DownloadEntity>
{
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
    }
    
    public CannedQuery<DownloadEntity> getDownloadsCannedQuery(NodeRef containerNode, Date before)
    {
        ParameterCheck.mandatory("before", before);
        
        GetDownloadsCannedQueryParams parameterBean = new GetDownloadsCannedQueryParams
                    (
                                getNodeId(containerNode),
                                getQNameId(ContentModel.PROP_NAME),
                                getQNameId(DownloadModel.TYPE_DOWNLOAD),
                                before
                    );
        CannedQueryParameters params = new CannedQueryParameters(parameterBean);

        final GetDownloadsCannedQuery cq = new GetDownloadsCannedQuery(
              cannedQueryDAO, methodSecurity, params
        );
        
        return cq;
    }

    /*
     * @see org.alfresco.query.CannedQueryFactory#getCannedQuery(org.alfresco.query.CannedQueryParameters)
     */
    @Override
    public CannedQuery<DownloadEntity> getCannedQuery(CannedQueryParameters parameters)
    {
        return new GetDownloadsCannedQuery(cannedQueryDAO, methodSecurity, parameters);
    }
}
