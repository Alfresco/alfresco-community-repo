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
package org.alfresco.repo.download.cannedquery;

import java.util.Date;

/**
 * Query parameters for GetDownloadsCannedQuery
 * 
 * @author Alex Miller
 */
public class GetDownloadsCannedQueryParams extends DownloadEntity
{
    private Date before;

    public GetDownloadsCannedQueryParams(Long parentNodeId, Long nameQNameId, Long contentTypeQNameId, Date before)
    {
        super(parentNodeId, nameQNameId, contentTypeQNameId);
        this.before = before;
    }

    public Date getBefore()
    {
        return before;
    }
}
