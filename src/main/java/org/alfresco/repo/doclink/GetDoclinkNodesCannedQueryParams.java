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

package org.alfresco.repo.doclink;

/**
 * Parameter object for {@link GetDoclinkNodesCannedQuery}.
 * 
 * @author Ramona Popa
 * @since 5.2.1
 */
public class GetDoclinkNodesCannedQueryParams
{

    private String parentNodeStringValue;
    private Long typeQNameId;
    private int limit;

    public GetDoclinkNodesCannedQueryParams(String parentNodeStringValue, Long typeQNameId, int limit)
    {
        this.setParentNodeStringValue(parentNodeStringValue);
        this.setTypeQNameId(typeQNameId);
        this.setLimit(limit);
    }

    public String getParentNodeStringValue()
    {
        return parentNodeStringValue;
    }

    public void setParentNodeStringValue(String parentNodeStringValue)
    {
        this.parentNodeStringValue = parentNodeStringValue;
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

}
