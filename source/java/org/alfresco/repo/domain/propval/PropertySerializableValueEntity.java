package org.alfresco.repo.domain.propval;

import java.io.Serializable;

import org.alfresco.util.Pair;

/**
 * Entity bean for <b>alf_prop_serializable_value</b> table.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class PropertySerializableValueEntity
{
    private Long id;
    private Serializable serializableValue;
    
    public PropertySerializableValueEntity()
    {
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("PropertySerializableValueEntity")
          .append("[ id=").append(id)
          .append(", value=").append(serializableValue)
          .append("]");
        return sb.toString();
    }
    
    /**
     * @return          Returns the ID-value pair
     */
    public Pair<Long, Serializable> getEntityPair()
    {
        return new Pair<Long, Serializable>(id, serializableValue);
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Serializable getSerializableValue()
    {
        return serializableValue;
    }

    public void setSerializableValue(Serializable serializableValue)
    {
        this.serializableValue = serializableValue;
    }    
}
