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
package org.alfresco.repo.solr;

/**
 * Filters for node metadata results e.g. include properties, aspect, ... or not
 * 
 * @since 4.0
 *
 */
public class MetaDataResultsFilter
{
    private boolean includeProperties = true;
    private boolean includeAspects = true;
    private boolean includeType = true;
    private boolean includeAclId = true;
    private boolean includeOwner = true;
    private boolean includePaths = true;
    private boolean includeParentAssociations = true;
    private boolean includeChildAssociations = true;
    private boolean includeNodeRef = true;
    private boolean includeChildIds = true;
    private boolean includeTxnId = true;
    
    public boolean getIncludeChildAssociations()
    {
        return includeChildAssociations;
    }
    public void setIncludeChildAssociations(boolean includeChildAssociations)
    {
        this.includeChildAssociations = includeChildAssociations;
    }
    public boolean getIncludeNodeRef()
    {
        return includeNodeRef;
    }
    public void setIncludeNodeRef(boolean includeNodeRef)
    {
        this.includeNodeRef = includeNodeRef;
    }
    public boolean getIncludeParentAssociations()
    {
        return includeParentAssociations;
    }
    public void setIncludeParentAssociations(boolean includeParentAssociations)
    {
        this.includeParentAssociations = includeParentAssociations;
    }
    public boolean getIncludeProperties()
    {
        return includeProperties;
    }
    public void setIncludeProperties(boolean includeProperties)
    {
        this.includeProperties = includeProperties;
    }
    public boolean getIncludeAspects()
    {
        return includeAspects;
    }
    public void setIncludeAspects(boolean includeAspects)
    {
        this.includeAspects = includeAspects;
    }
    public boolean getIncludeType()
    {
        return includeType;
    }
    public void setIncludeType(boolean includeType)
    {
        this.includeType = includeType;
    }
    public boolean getIncludeAclId()
    {
        return includeAclId;
    }
    public void setIncludeAclId(boolean includeAclId)
    {
        this.includeAclId = includeAclId;
    }
    public boolean getIncludeOwner()
    {
        return includeOwner;
    }
    public void setIncludeOwner(boolean includeOwner)
    {
        this.includeOwner = includeOwner;
    }
    public boolean getIncludePaths()
    {
        return includePaths;
    }
    public void setIncludePaths(boolean includePaths)
    {
        this.includePaths = includePaths;
    }
    public boolean getIncludeChildIds()
    {
        return includeChildIds;
    }
    public void setIncludeChildIds(boolean includeChildIds)
    {
        this.includeChildIds = includeChildIds;
    }    
    public boolean getIncludeTxnId()
    {
        return includeTxnId;
    }
    public void setIncludeTxnId(boolean includeTxnId)
    {
        this.includeTxnId = includeTxnId;
    }  
}
