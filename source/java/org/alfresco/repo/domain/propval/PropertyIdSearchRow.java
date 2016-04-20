package org.alfresco.repo.domain.propval;

import java.io.Serializable;

/**
 * Entity bean search results from <b>alf_prop_collections_link</b> and <b>alf_prop_value</b> tables.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertyIdSearchRow
{
    private final PropertyLinkEntity linkEntity;
    private final PropertyValueEntity valueEntity;
    
    public PropertyIdSearchRow()
    {
        linkEntity = new PropertyLinkEntity();
        valueEntity = new PropertyValueEntity();
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertyIdSearchRow")
          .append("[ ").append(linkEntity)
          .append(", ").append(valueEntity)
          .append("]");
        return sb.toString();
    }

    public PropertyLinkEntity getLinkEntity()
    {
        return linkEntity;
    }

    public PropertyValueEntity getValueEntity()
    {
        return valueEntity;
    }

    public void setRootPropId(Long rootPropId)
    {
        linkEntity.setRootPropId(rootPropId);
    }

    public void setPropIndex(Long propIndex)
    {
        linkEntity.setPropIndex(propIndex);
    }

    public void setContainedIn(Long containedIn)
    {
        linkEntity.setContainedIn(containedIn);
    }

    public void setKeyPropId(Long keyPropId)
    {
        linkEntity.setKeyPropId(keyPropId);
    }
    
    public void setValuePropId(Long valuePropId)
    {
        linkEntity.setValuePropId(valuePropId);
    }

    public void setActualTypeId(Long actualTypeId)
    {
        valueEntity.setActualTypeId(actualTypeId);
    }

    public void setPersistedType(Short persistedType)
    {
        valueEntity.setPersistedType(persistedType);
    }

    public void setLongValue(Long longValue)
    {
        valueEntity.setLongValue(longValue);
    }

    public void setStringValue(String stringValue)
    {
        valueEntity.setStringValue(stringValue);
    }

    public void setDoubleValue(Double doubleValue)
    {
        valueEntity.setDoubleValue(doubleValue);
    }

    public void setSerializableValue(Serializable serializableValue)
    {
        valueEntity.setSerializableValue(serializableValue);
    }
}
