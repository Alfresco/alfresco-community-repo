package org.alfresco.repo.domain.patch.ibatis;

/**
 * Parameters for the <b>cm:sizeCurrent</b> update.
 */
public class SizeCurrentParams
{
    private Long sizeCurrentQNameId;
    private Long personTypeQNameId;
    private Long defaultLocaleId;

    public boolean getFalse()
    {
        return false;
    }
    
    public Long getSizeCurrentQNameId()
    {
        return sizeCurrentQNameId;
    }
    public void setSizeCurrentQNameId(Long sizeCurrentQNameId)
    {
        this.sizeCurrentQNameId = sizeCurrentQNameId;
    }
    public Long getPersonTypeQNameId()
    {
        return personTypeQNameId;
    }
    public void setPersonTypeQNameId(Long personTypeQNameId)
    {
        this.personTypeQNameId = personTypeQNameId;
    }
    public Long getDefaultLocaleId()
    {
        return defaultLocaleId;
    }
    public void setDefaultLocaleId(Long defaultLocaleId)
    {
        this.defaultLocaleId = defaultLocaleId;
    }
}
