package org.alfresco.repo.domain.propval;

/**
 * Entity bean for <b>alf_prop_link</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyLinkEntity
{
    private Long rootPropId;
    private Long propIndex;
    private Long containedIn;
    private Long keyPropId;
    private Long valuePropId;
    
    public PropertyLinkEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyLinkEntity")
          .append("[ rootPropId=").append(rootPropId)
          .append(", propIndex=").append(propIndex)
          .append(", containedIn=").append(containedIn)
          .append(", keyPropId=").append(keyPropId)
          .append(", valuePropId=").append(valuePropId)
          .append("]");
        return sb.toString();
    }

    public Long getRootPropId()
    {
        return rootPropId;
    }

    public void setRootPropId(Long rootPropId)
    {
        this.rootPropId = rootPropId;
    }

    public Long getPropIndex()
    {
        return propIndex;
    }

    public void setPropIndex(Long propIndex)
    {
        this.propIndex = propIndex;
    }

    public Long getContainedIn()
    {
        return containedIn;
    }

    public void setContainedIn(Long containedIn)
    {
        this.containedIn = containedIn;
    }

    public Long getKeyPropId()
    {
        return keyPropId;
    }

    public void setKeyPropId(Long keyPropId)
    {
        this.keyPropId = keyPropId;
    }

    public Long getValuePropId()
    {
        return valuePropId;
    }

    public void setValuePropId(Long valuePropId)
    {
        this.valuePropId = valuePropId;
    }
}
