package org.alfresco.repo.domain.solr;

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
    private boolean includeAssociations = true;
    private boolean includeNodeRef = true;
    
    public boolean getIncludeNodeRef()
    {
        return includeNodeRef;
    }
    public void setIncludeNodeRef(boolean includeNodeRef)
    {
        this.includeNodeRef = includeNodeRef;
    }
    public boolean getIncludeAssociations()
    {
        return includeAssociations;
    }
    public void setIncludeAssociations(boolean includeAssociations)
    {
        this.includeAssociations = includeAssociations;
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
    
}
