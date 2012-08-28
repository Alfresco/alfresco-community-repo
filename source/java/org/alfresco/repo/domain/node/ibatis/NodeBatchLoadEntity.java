/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.domain.node.ibatis;

import java.util.List;

/**
 * Bean to convey carry query information of node batch loading.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class NodeBatchLoadEntity
{
    private Long storeId;
    private List<String> uuids;
    private List<Long> ids;
    
    public Long getStoreId()
    {
        return storeId;
    }
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }
    public List<String> getUuids()
    {
        return uuids;
    }
    public void setUuids(List<String> uuids)
    {
        this.uuids = uuids;
    }
    public List<Long> getIds()
    {
        return ids;
    }
    public void setIds(List<Long> ids)
    {
        this.ids = ids;
    }
}
