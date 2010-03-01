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
package org.alfresco.repo.domain.avm;

import java.io.Serializable;

/**
 * Entity bean for <b>avm_version_layered_node_entry</b> table
 * 
 * @author janv
 * @since 3.2
 */
public class AVMVersionLayeredNodeEntryEntity
{
    private Long versionRootId;
    private String md5sum;
    private String path;
    
    public Long getVersionRootId()
    {
        return versionRootId;
    }
    
    public void setVersionRootId(Long versionRootId)
    {
        this.versionRootId = versionRootId;
    }
    
    public String getMd5sum()
    {
        return md5sum;
    }
    
    public void setMd5sum(String md5sum)
    {
        this.md5sum = md5sum;
    }
    
    public String getPath()
    {
        return path;
    }
    
    public void setPath(String path)
    {
        this.path = path;
    }
}
